# Attack Surface Map — TODO

**Lesson 1 — HTTP, JSON, and the Spring MVC Request Lifecycle**

Map every untrusted-input entry point in the baseline app. For each one, record
the **trust level** of the data arriving there and the **risk** it introduces
while the application is undefended.

> Tip: trace one full request through the Spring MVC lifecycle — dispatcher →
> handler mapping → argument resolution / JSON deserialization → your handler
> method. Untrusted input enters at more than one place.

## Entry points

| # | Entry point | Trust level | Risk |
|---|-------------|-------------|------|
| 1 | HTTP request to the application            | Untrusted            |  The client controls the incoming request and can send arbitrary data to the application.    |
| 2 |   Request method and path (POST /api/transactions)          |   Untrusted          | The client controls the HTTP method and requested path. Attackers can probe the application's exposed routes and attempt unexpected requests.     |
| 3 |     JSON request body        |    Untrusted         |   The entire request body is attacker-controlled. It can contain missing, unexpected, malformed, or malicious values.   |
| 4 |     TransactionRequest JSON deserialization        |    Untrusted         |    Jackson converts the attacker-controlled JSON into a Java object, but no validation is performed. Successful deserialization does not mean the values are safe or valid.  |
| 5 |   accountId field          |   Untrusted          |   The caller can supply any string. Without validation or authorization, an attacker could provide an invalid, unexpected, or malicious account identifier.   |
| 6 |     amount field        |      Untrusted       |   The caller controls the BigDecimal value. No business validation prevents unexpected values such as negative, zero, excessively large, or unusually precise amounts.   |
| 7 |    currency field         |     Untrusted        |  The caller can supply any string. No validation ensures that it is a supported currency or otherwise conforms to an expected format.    |
| 8 |         memo field    |      Untrusted       |   The caller can supply arbitrary text. Without validation or output-specific handling, this could contain excessive or malicious content if the value is later used by another component.   |

## Notes / reasoning

_(Use this space to explain how you traced each entry point through the request
lifecycle, and any assumptions you made about how the data is used downstream.)_

1. The attack surface begins when a client sends an HTTP request to the Spring Boot application. The client is outside the application's trust boundary, so everything originating from the client must initially be considered untrusted.

2. The controller exposes /api/health and /api/transactions through @GetMapping and @PostMapping. The client controls which HTTP method and path it sends. An attacker can therefore probe the application's available endpoints and attempt requests against them.

3. The createTransaction() method accepts a request body through @RequestBody. The JSON body is supplied entirely by the client, so its contents cannot be trusted. The attacker can control which fields are present and what values they contain.

4. Spring MVC uses the @RequestBody parameter and Jackson to deserialize the JSON into a TransactionRequest object. This converts the external JSON representation into Java values, but the TransactionRequest class contains no validation annotations or validation logic. Successful deserialization therefore does not make the data trusted.

5. accountId is a String populated from the JSON request. The client can supply any string because there is no validation in TransactionRequest. If this value were later used in a database query, authorization decision, log message, or other sensitive operation, the lack of validation could create security risk.
   
6. amount is converted from the JSON request into a BigDecimal. The type conversion establishes that the value can be represented as a BigDecimal, but there are no business or security checks on the value. For example, negative, zero, extremely large, or unusually precise amounts are not rejected by this DTO.

7. currency is a String supplied by the client. There is no validation requiring it to be a supported currency code or a particular format. Therefore, arbitrary input can reach the controller.

8. memo is also a client-controlled String with no validation. The current controller does not include memo in its response, but the field is still part of the application's input surface. If it were later logged, stored, displayed, or passed to another component, malicious or excessively large content could create additional risk.

   
## Annotated request lifecycle
DispatcherServlet -> HandlerMapping -> HandlerAdapter -> Controller method

DispatcherServlet: DispatcherServlet receives the POST request and passes it through Spring MVC.
HandlerMapping: HandlerMapping matches POST /api/transactions to createTransaction() in TransactionController.
HandlerAdapter: HandlerAdapter prepares the TransactionRequest argument, as @RequestBody tells Spring to read the request body. MappingJackson2HttpMessageConverter uses Jackson to convert the JSON into the object’s fields.
Controller method: Controller reads accountId, amount, and currency and returns a 201 acknowledgment without validating the values.