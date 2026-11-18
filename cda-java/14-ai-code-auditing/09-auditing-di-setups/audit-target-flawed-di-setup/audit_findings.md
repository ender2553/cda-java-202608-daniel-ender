Systematic DI Security Audit — Flawed Spring Orders Service
Audit target: audit-target-flawed-di-setup
Framework: Spring Boot 3.2.5
Java: 17
Audit method: Four-step DI-audit pass
Files reviewed: 12

The audit is complete. There are several significant defects involving attacker-controlled dependency selection, shared mutable state, and excessive privileges granted through dependency injection.

The important distinction is that these defects are not all visible by looking at individual methods. Some become apparent only when tracing the entire dependency graph and examining the container's bean lifecycles.

Pass 1 — Enumerate Every DI Binding
First, establish the complete inventory before identifying vulnerabilities.
Bean inventory
| Class / Binding | Annotation | Scope | Injected Dependencies |
|---|---|---|---|
| `OrdersApplication` | `@SpringBootApplication` | Singleton | None |
| `PricingConfig` | `@Configuration` | Singleton | None |
| `AdminOrderRepository` | `@Repository` | Singleton | None |
| `CartHolder` | `@Component` | Singleton | None |
| `CheckoutController` | `@RestController` | Singleton | CartHolder, OrderContextCache, PricingStrategy, InternalLedgerService |
| `InternalLedgerService` | `@Service` | Singleton | None |
| `OrderContextCache` | `@Component` | Singleton | None |
| `OrderStatusController` | `@RestController` | Singleton | AdminOrderRepository |
| `pricingStrategy` | `@Bean` | Request | HttpServletRequest |


Additional implementation:
- PricingStrategy — interface, not independently registered as a bean.
- StandardPricingStrategy — manually instantiated inside PricingConfig; not independently registered as a component.
Total: 9 container-managed bindings, including the application and configuration classes.

The inventory exposes two particularly important areas for further examination:
1. The request-scoped pricing strategy.
2. The singleton state holders.

Pass 2 — Trace Implementation Provenance
Question: Where does each implementation come from, and who controls its selection?
Finding 1 — Attacker-controlled pricing strategy injection
| Field | Finding |
|---|---|
| **Location** | `PricingConfig.java` — `pricingStrategy()` |
| **Pass** | Step 2: Trace provenance |
| **Defect class** | Untrusted injection |
| **Severity** | Critical |


Problem:
String strategyClass = request.getHeader("X-Pricing-Strategy");

return (PricingStrategy) Class.forName(strategyClass)
        .getDeclaredConstructor()
        .newInstance();

The implementation is selected using an HTTP request header.

An external caller controls:

X-Pricing-Strategy

The application then dynamically loads and instantiates the requested class.

Concrete risk:

An attacker can attempt to select any class on the application's classpath that implements PricingStrategy and has an accessible no-argument constructor.

A malicious implementation could:
- Return artificially low prices.
- Return zero or negative prices.
- Return excessively high prices.
- Execute unwanted code during construction or pricing.
- Introduce additional unintended behavior.
The container effectively allows an HTTP caller to influence which implementation gets injected into the checkout process.

Recommended fix:

Replace dynamic class loading with a fixed, trusted allow-list.

For example:
@Bean
public PricingStrategy pricingStrategy() {
    return new StandardPricingStrategy();
}

If multiple strategies are necessary, select among explicitly registered, trusted implementations using a validated business identifier.

Never use request headers as class names.
Pass 3 — Check Scope and Lifecycle
Question: Does each bean's lifetime match the state it carries?

This pass reveals two significant shared-state vulnerabilities and one additional lifecycle problem.
Finding 2 — CartHolder shares customer cart data across requests

| Field | Finding |
|---|---|
| **Location** | `CartHolder.java` — `@Component` |
| **Pass** | Step 3: Check scope |
| **Defect class** | Scope/lifecycle defect |
| **Severity** | Critical |


Problem:
@Component
public class CartHolder {

    private final List<String> items = new ArrayList<>();

Spring components are singleton-scoped by default.

Consequently, every customer shares the same CartHolder instance and the same items list.

Concrete risk:

Consider two customers:
1. Customer A adds two products.
2. Customer B adds three products.
3. Customer B checks out.
The shared cart now contains five products.

Checkout calculates:
long total = pricingStrategy.priceCents(cartHolder.items().size());

Customer B could therefore be charged for Customer A's products.

Additional problems include:
- Cross-customer cart information leakage.
- Race conditions during concurrent requests.
- Unauthorized modification of another customer's cart.
The items() method also exposes the underlying mutable list.

Recommended fix:

Use request-isolated or explicitly customer/session-scoped cart state.

Preferably:
- Keep cart state associated with an authenticated customer.
- Avoid storing customer-specific state in singleton services.
- Return immutable copies instead of exposing the internal list.
Finding 3 — OrderContextCache permanently shares customer identity

| Field | Finding |
|---|---|
| **Location** | `OrderContextCache.java` — `@Component` |
| **Pass** | Step 3: Check scope |
| **Defect class** | Scope/lifecycle defect |
| **Severity** | Critical |


Problem:
@Component
public class OrderContextCache {

    private String customerId;

    public String resolveCustomer(String incomingCustomerId) {
        if (customerId == null) {
            customerId = incomingCustomerId;
        }
        return customerId;
    }
}

The component is a singleton and stores mutable customer-specific state.

The first customer ID is retained indefinitely.

Subsequent requests receive that same customer ID, regardless of who submitted them.

Concrete risk:
1. Customer A checks out using ID A123.
2. The cache stores A123.
3. Customer B checks out using ID B456.
4. The cache still returns A123.
5. Customer B's checkout is posted against Customer A's account.
This creates a serious financial integrity problem.

Concurrent requests also introduce race conditions when multiple customers attempt the initial assignment.

Recommended fix:

Remove the shared mutable customer ID.

Resolve customer identity independently for each request using the authenticated user's identity or a properly validated customer context.

Do not use a singleton to retain per-customer identity.
Finding 4 — Request-scoped pricing strategy lacks a scoped proxy

| Field | Finding |
|---|---|
| **Location** | `PricingConfig.java` — `pricingStrategy()` |
| **Pass** | Step 3: Check scope |
| **Defect class** | Scope/lifecycle defect |
| **Severity** | High |


Problem:

The pricing strategy is declared:
@Bean
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public PricingStrategy pricingStrategy(HttpServletRequest request)

However, CheckoutController is a singleton and directly injects this request-scoped bean:
private final PricingStrategy pricingStrategy;

There is no scoped proxy configured.

Concrete risk:

Spring attempts to resolve the request-scoped dependency while constructing the singleton controller.

Because no HTTP request scope is active during normal singleton initialization, the application can fail during startup with a ScopeNotActiveException.

Even if the dependency were resolved through an appropriate proxy, the current implementation-selection vulnerability would remain.

Recommended fix:

Either:
- Make pricing a trusted singleton if it carries no request-specific state; or
- Configure a request-scoped proxy using ScopedProxyMode.TARGET_CLASS or an appropriate interface-based proxy.
For this application, a trusted singleton pricing strategy is the simpler solution.
Pass 4 — Verify Dependency Boundaries
Question: What can each consumer reach through its injected dependencies, rather than merely what methods does it currently call?

This pass reveals excessive privileges and missing security boundaries.
Finding 5 — CheckoutController receives excessive financial privileges

| Field | Finding |
|---|---|
| **Location** | `CheckoutController.java` — `InternalLedgerService ledgerService` |
| **Pass** | Step 4: Verify boundaries |
| **Defect class** | Over-broad wiring |
| **Severity** | Critical |


Problem:
private final InternalLedgerService ledgerService;

The controller only needs:
ledgerService.postEntry(customer, total);

However, the injected service also exposes:
public void adjustBalance(String account, long deltaCents)

public long readBalance(String account)

The checkout controller therefore receives access to privileged financial operations it does not need.

Concrete risk:

If the checkout controller develops an injection vulnerability, logic flaw, or compromised execution path, the attacker could potentially access:
- Manual balance adjustments.
- Financial balance information.
- Ledger posting operations.
The controller has more financial authority than its responsibility warrants.

Recommended fix:

Create a narrow interface:
public interface LedgerPostingService {
    void postEntry(String account, long amountCents);
}

Inject that interface into CheckoutController.

Keep balance adjustments and privileged financial operations behind a separate administrative service with appropriate authorization.
Finding 6 — OrderStatusController receives administrative repository privileges

| Field | Finding |
|---|---|
| **Location** | `OrderStatusController.java` — `AdminOrderRepository orderRepository` |
| **Pass** | Step 4: Verify boundaries |
| **Defect class** | Over-broad wiring |
| **Severity** | High |


Problem:
private final AdminOrderRepository orderRepository;

The controller only needs:
orderRepository.findStatus(id);

However, the repository also exposes:
deleteOrder(String orderId)

overrideStatus(String orderId, String status)

refund(String orderId)

A status-reading controller has access to administrative order-management operations.

Concrete risk:

A vulnerability in the controller could potentially allow an attacker to:
- Delete orders.
- Override order statuses.
- Trigger refunds.
The controller has administrative capabilities despite only needing read access.

Recommended fix:

Create a dedicated read-only interface:
public interface OrderStatusReader {
    String findStatus(String orderId);
}

Inject that interface into OrderStatusController.

Keep deletion, refunds, and status overrides behind a separate administrative service with authorization checks.
Finding 7 — Missing authentication and authorization boundary

| Field | Finding |
|---|---|
| **Location** | `CheckoutController.java` and `OrderStatusController.java` |
| **Pass** | Step 4: Verify boundaries |
| **Defect class** | Missing access boundary |
| **Severity** | Critical |


Problem:

The supplied project has only:
<artifactId>spring-boot-starter-web</artifactId>

There is no Spring Security dependency or visible authentication/authorization configuration.

Both controllers expose HTTP endpoints:
@PostMapping("/checkout")

@GetMapping("/orders/{id}/status")

Neither endpoint demonstrates an authentication or authorization requirement.

Concrete risk:

Without an external security boundary, arbitrary HTTP callers can potentially:
- Submit checkout requests.
- Supply customer IDs.
- Supply product SKUs.
- Trigger financial ledger postings.
- Query order statuses.
The checkout endpoint also accepts customer identity directly from request parameters rather than deriving it from an authenticated principal.

Recommended fix:

Implement Spring Security with:
- Authentication for all sensitive endpoints.
- Authorization based on customer ownership and appropriate roles.
- Customer identity derived from the authenticated principal.
- Additional authorization for administrative operations.
- Validation of order ownership before returning order information.
Do not trust client-supplied customer IDs as proof of identity.

| # | Location | Pass | Defect Class | Risk |
|---|---|---|---|---|
| 1 | `PricingConfig.pricingStrategy()` | 2 | Untrusted injection | HTTP caller selects dynamically instantiated pricing implementation. |
| 2 | `CartHolder` | 3 | Scope/lifecycle defect | Singleton cart shares products across customers; incorrect charges and data leakage. |
| 3 | `OrderContextCache` | 3 | Scope/lifecycle defect | Singleton retains first customer ID, potentially charging subsequent customers against the wrong account. |
| 4 | `PricingConfig.pricingStrategy()` | 3 | Scope/lifecycle defect | Request-scoped bean injected directly into singleton controller without proxy; application startup can fail. |
| 5 | `CheckoutController.ledgerService` | 4 | Over-broad wiring | Checkout controller receives privileged financial adjustment and balance-reading capabilities. |
| 6 | `OrderStatusController.orderRepository` | 4 | Over-broad wiring | Read-only controller receives order deletion, refund, and status-override capabilities. |
| 7 | Both controllers | 4 | Missing access boundary | Sensitive HTTP operations lack visible authentication, authorization, and customer ownership enforcement. |

Overall Audit Assessment
Seven findings identified across the four-pass audit.

| Defect Category | Findings |
|---|---:|
| Untrusted injection | 1 |
| Scope/lifecycle defects | 3 |
| Over-broad wiring | 2 |
| Missing access boundary | 1 |
| **Total** | **7** |


Most important security problems
1. Untrusted implementation selection: An HTTP header determines which pricing implementation gets instantiated.
2. Cross-customer state leakage: Two singleton components retain customer-specific state.
3. Excessive financial privileges: The checkout controller can reach administrative ledger operations.
4. Excessive repository privileges: The status controller can reach order-management operations.
5. Missing authentication: Sensitive operations lack visible access controls.
Important runtime observation
Finding #4 deserves special attention: the request-scoped pricing bean is injected into a singleton without a scoped proxy. This can prevent the application from starting successfully.

That is an additional problem beyond the pricing strategy's untrusted provenance.
Final conclusion
The central problem is that the application treats dependency injection as convenient object wiring rather than a security boundary.

Three principles would prevent most of these defects:
- Trust: Never let an untrusted HTTP request select an implementation class.
- Isolation: Never store per-customer or per-request state in shared singleton beans.
- Least privilege: Inject only the capabilities a component actually needs.
Audit takeaway: A DI security audit must examine not only what a component does, but also where its dependencies come from, how long they live, and everything they allow that component to reach.