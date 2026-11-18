Customer Portal — Combined Configuration & Dependency Injection Security Audit
Audit verdict: NOT SAFE TO DEPLOY

The application builds and boots, but it has multiple critical security weaknesses. The most serious problem is that the application effectively has no access-control boundary: application endpoints and Actuator endpoints are accessible without authentication.

There are also configuration weaknesses that become exploitable when combined with the dependency-injection wiring, particularly reflective class loading, JWT handling, and shared mutable singleton state.

The following report covers both passes and their intersections.

1. Configuration Security Pass
Finding C1 — Hardcoded database credentials
Location: application.yml

spring:
  
  datasource:
    
    username: portal_app
    
    password: P0rtal-Db-2024!

Defect class: Hardcoded secrets / credential management.

Risk:
- Database credentials are stored in plaintext configuration.
- Anyone with repository or configuration access can obtain them.
- If the repository is exposed, an attacker can attempt direct database access.
- Credentials may also be copied into backups, logs, and deployment artifacts.
Recommended fix:
- Store credentials in environment variables or a secrets manager.
- Rotate the exposed database password.
- Apply least-privilege permissions to the database account.

Finding C2 — Hardcoded JWT secret and insecure token implementation
Location: application.yml

portal:
  
  jwt:
    
    secret: 8f14e45fceea167a5a36dedd4bea2543c1d4e3b7a9f02
    
    ttl-seconds: 3600

Defect class: Hardcoded cryptographic secret / insecure authentication design.

Risk:
- The signing secret is embedded in configuration.
- Anyone obtaining the configuration can recover the secret.
- The token implementation also uses Java's String.hashCode() rather than cryptographic signing.
- Tokens are predictable and can be forged.
- The TTL is included as a number rather than enforced as an expiration timestamp.
Recommended fix:
- Generate a strong random signing key.
- Store it outside the application configuration.
- Use a reputable JWT library with HMAC-SHA256 or an appropriate asymmetric algorithm.
- Include and validate expiration claims.
- Implement signature verification.
Cross-layer finding: See D4.

Finding C3 — HTTPS requirement is configured using an invalid property
Location: application.yml

spring:
  
  security:
    
    require-ssl: true

Defect class: Invalid framework configuration / ineffective security control.

Risk:

This looks like an SSL enforcement setting, but spring.security.require-ssl is not a recognized Spring Boot 3.2.5 application property.

Consequently, the application does not enforce HTTPS through this setting.

An attacker who can reach the application over an unencrypted connection could potentially intercept credentials, tokens, and application data.

Recommended fix:
- Configure TLS through the appropriate server.ssl.* properties or the deployment ingress.
- Enforce HTTPS redirects or HTTPS-only access through Spring Security.
- Configure secure proxy handling where applicable.
- Verify that HTTP requests cannot reach sensitive endpoints.
Cross-layer finding: SecurityConfig does not provide an alternative HTTPS enforcement mechanism.

Finding C4 — Stack traces and exception messages exposed
Location: application.yml

server:
  
  error:
    
    include-stacktrace: always
    
    include-message: always

Defect class: Information disclosure.

Risk:
- Remote users can receive detailed exception messages.
- Stack traces can expose internal package names, class names, application structure, and implementation details.
- Database errors and other exception messages may reveal sensitive information.
- Attackers can use the information to develop more targeted attacks.
Recommended fix:

server:
  
  error:
    
    include-stacktrace: never
    
    include-message: never

Keep detailed diagnostic information in protected server logs.

Finding C5 — All Actuator endpoints exposed

Location: application.yml

management:
  
  endpoints:
    
    web:
     
     exposure:
        
        include: "*"
  
  endpoint:
    
    health:
      
      show-details: always

Defect class: Excessive management-interface exposure / information disclosure.

Risk:

The wildcard exposes all available Actuator endpoints over HTTP.

Depending on the endpoints available, this can expose:
- Environment and configuration information.
- Application bean information.
- Request mappings.
- Metrics and operational information.
- Thread dumps.
- Heap dumps containing sensitive in-memory information.
Additionally, show-details: always exposes detailed health information.

Recommended fix:

Expose only necessary endpoints:

management:
  
  endpoints:
   
    web:
      
      exposure:
        
        include: health,info
  
  endpoint:
    
    health:
      
      show-details: never

Protect management endpoints with separate authorization rules and preferably network restrictions.

Cross-layer finding: SecurityConfig makes the exposed endpoints publicly accessible.

Finding C6 — Shutdown endpoint explicitly enabled

Location: application.yml

management:
  
  endpoint:
    
    shutdown:
      
      enabled: true

Defect class: Dangerous administrative functionality exposed remotely.

Risk:
- Spring Boot normally disables the shutdown endpoint.
- This configuration explicitly enables it.
- Combined with wildcard endpoint exposure and the permissive security filter chain, an unauthenticated remote attacker can potentially shut down the application.
This creates a direct availability attack.

Recommended fix:

Remove the setting or explicitly disable it:

management:
  
  endpoint:
    
    shutdown:
      
      enabled: false

If administrative shutdown functionality is genuinely necessary, restrict it to authenticated administrators and a protected management network.

Cross-layer finding: See D2.

Finding C7 — Application binds to every network interface

Location: application.yml

server:
  
  address: 0.0.0.0
  
  port: 8080

Defect class: Excessive network exposure.

Risk:
- The application listens on all available network interfaces.
- This can expose the service beyond its intended internal network.
- Combined with the absence of authentication, this substantially increases attack accessibility.
Recommended fix:
- Bind to an appropriate internal interface where possible.
- Restrict access through firewall, container networking, or ingress rules.
- Keep administrative endpoints on a separate management interface or network.
Note: Binding to 0.0.0.0 is not inherently a vulnerability in container deployments, but it requires appropriate network controls.

Finding C8 — Unsupported Spring Boot dependency version

Location: pom.xml

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>

Defect class: Outdated framework / dependency security maintenance.

Risk:
- Spring Boot 3.2.5 is an old release.
- Spring Boot 3.2.x reached the end of open-source support with version 3.2.12 in November 2024.
- The application no longer receives normal community security updates through that release line.
An attacker may exploit known vulnerabilities in outdated framework dependencies.

Recommended fix:
- Upgrade to a currently supported Spring Boot release.
- Review the dependency tree for vulnerable transitive dependencies.
- Run dependency vulnerability scanning as part of CI/CD.

2. Dependency Injection Audit

Step 1 — Enumerate all managed bindings

The application contains the following managed components and configuration bindings.

| Bean / Component | Binding source | Default scope |
|---|---|---|
| PortalApplication | `@SpringBootApplication` | Singleton |
| AdminUserRepository | `@Repository` | Singleton |
| CurrentUserService | `@Service` | Singleton |
| InternalAuditService | `@Service` | Singleton |
| JwtTokenService | `@Service` | Singleton |
| ProfileController | `@RestController` | Singleton |
| SecurityConfig | `@Configuration` | Singleton |
| NotifierConfig | `@Configuration` | Singleton |
| SecurityFilterChain | `@Bean` in SecurityConfig | Singleton |
| Notifier | `@Bean` in NotifierConfig | Singleton |


Important observation:

EmailNotifier is not annotated with @Component, @Service, or another stereotype.

It becomes a managed bean through reflective construction in NotifierConfig.

The Notifier interface itself is not a separate bean.

Step 2 — Trace provenance, scope, and boundaries

Finding D1 — Global authorization disabled

Location: SecurityConfig.java

.authorizeHttpRequests(auth -> auth
    
    .anyRequest().permitAll()

);

Defect class: Broken authorization / missing authentication boundary.

Risk:

Every HTTP request is permitted without authentication.

An unauthenticated remote attacker can potentially:
- Read customer profiles.
- Submit notification requests.
- Access exposed Actuator endpoints.
- Invoke any subsequently added application endpoints without authorization.
There is no meaningful role-based access control.

Recommended fix:
- Require authentication by default.
- Define explicit public endpoints.
- Require appropriate roles for customer and administrative operations.
- Apply method-level authorization where appropriate.

For example:

.authorizeHttpRequests(auth -> auth
    
    .requestMatchers("/actuator/health").permitAll()
    
    .requestMatchers("/admin/**").hasRole("ADMIN")
    
    .anyRequest().authenticated()

);

Finding D2 — CSRF protection disabled

Location: SecurityConfig.java

.csrf(csrf -> csrf.disable())

Defect class: Disabled request-forgery protection.

Risk:

CSRF protection is disabled globally.

The application currently has no effective authentication mechanism, so a conventional authenticated-session CSRF attack is not established by the supplied code.

However, if cookie-based authentication is introduced, an attacker could potentially trick an authenticated user's browser into submitting unauthorized state-changing requests.

The shutdown and other administrative endpoints also require appropriate request protection.

Recommended fix:
- Keep CSRF enabled for browser-based, cookie-authenticated applications.
- Disable it only when the application is genuinely stateless and uses an appropriate non-cookie authentication mechanism.
- Configure security separately for browser and API clients if necessary.
Finding D3 — Shared mutable singleton causes cross-user data leakage

Location: CurrentUserService.java

@Service

public class CurrentUserService {

    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}

Defect class: Incorrect bean scope / shared mutable state / race condition.

Risk:

Spring services are singleton-scoped by default.

Therefore, every request shares the same CurrentUserService instance and the same userId field.

Consider two concurrent requests:
1. Request A sets userId = alice.
2. Request B sets userId = bob.
3. Request A retrieves userId.
4. Request A receives bob.

This can cause:
- One user receiving another user's profile.
- Audit records attributing actions to the wrong user.
- Cross-request identity confusion.

This is a particularly serious DI mistake because the bean's lifetime does not match the state it carries.

Recommended fix:

Do not store request-specific identity in a mutable singleton.

Pass the user identity as a method parameter or retrieve it from Spring Security's authenticated security context.

Prefer:

String profile = userRepository.findProfile(userId);

Also derive the authenticated identity from the security context rather than trusting a request parameter.

Finding D4 — JWT service does not perform cryptographic signing

Location: JwtTokenService.java

public String issue(String userId) {
    
    return userId + "." + secret.hashCode() + "." + ttlSeconds;

}

Defect class: Weak cryptography / token forgery.

Risk:

The service receives its secret through constructor injection:

@Value("${portal.jwt.secret}") String secret

However, the implementation uses:

secret.hashCode()

Java's String.hashCode() is not a cryptographic signing algorithm.

An attacker who understands the token format can construct predictable token values.

Furthermore:
- There is no JWT structure.
- There is no cryptographic signature.
- There is no expiration timestamp.
- There is no token verification implementation.
The configured TTL does not actually enforce expiration.

Recommended fix:
- Use a maintained JWT library.
- Sign tokens cryptographically.
- Validate signatures before accepting tokens.
- Enforce expiration and other required claims.
- Avoid implementing authentication cryptography manually.
Cross-layer finding: C2 and D4 are the same underlying weakness viewed from configuration and DI provenance.

The configuration supplies the secret, but the consuming bean does not use it securely.

Finding D5 — Reflective class loading from configuration

Location: NotifierConfig.java

@Bean

public Notifier notifier(
        
        @Value("${portal.notifier.class}") String notifierClass)
        
        throws Exception {

    
    return (Notifier) Class.forName(notifierClass)
        
        .getDeclaredConstructor()
        
        .newInstance();

}

Defect class: Unsafe dynamic class loading / uncontrolled implementation selection.

Risk:

The implementation class is selected through configuration.

The configuration currently specifies:

portal:
  
  notifier:
    
    class: com.acme.portal.EmailNotifier

However, Spring configuration values can come from external property sources, including environment variables and command-line overrides.

If an attacker or untrusted deployment process can influence this property, they may select another class.

Class.forName() loads the specified class, and the constructor is invoked.

This creates a potential arbitrary class-instantiation vulnerability under the application's privileges.

The problem is not simply that reflection is used. It is that the implementation choice is controlled by an insufficiently constrained input.

Recommended fix:

Prefer explicit Spring-managed implementations:

@Bean

public Notifier notifier() {
    
    return new EmailNotifier();

}

Alternatively:
- Use a fixed allowlist of permitted implementations.
- Validate the class against that allowlist.
- Avoid accepting arbitrary class names.
- Keep deployment configuration under trusted administrative control.

Cross-layer finding: The vulnerability exists through the combination of portal.notifier.class in application.yml and reflective construction in NotifierConfig.java.

Finding D6 — Repository exposes excessive capabilities

Location: AdminUserRepository.java

public void updateProfile(String userId, String profileJson)

public void deleteUser(String userId)

public void grantRole(String userId, String role)

Defect class: Excessive dependency capabilities / insufficient privilege separation.

Risk:

ProfileController receives the concrete AdminUserRepository:

private final AdminUserRepository userRepository;

This gives the controller access to all public repository operations, including:
- Updating profiles.
- Deleting users.
- Granting roles.

However, the controller currently needs only profile-reading functionality.

The dependency therefore provides more authority than the controller's stated responsibility requires.

An unsafe future endpoint or controller modification could expose administrative operations through an ordinary customer-facing controller.

Recommended fix:
- Separate read-only and administrative repository interfaces.
- Inject only the required interface.
- Protect role changes and user deletion behind dedicated administrative services.
- Enforce authorization at the service layer.

Finding D7 — Unsafe JSON construction in role assignment

Location: AdminUserRepository.java

public void grantRole(String userId, String role) {
    
    profiles.put(userId, "{\"role\":\"" + role + "\"}");

}

Defect class: Improper input encoding / JSON injection.

Risk:

The role value is concatenated directly into JSON.

If an attacker-controlled value reaches this method, it could introduce unexpected JSON structure or produce malformed JSON.

This could undermine role interpretation by downstream components.

There is no direct HTTP route invoking this method in the supplied controller, so remote exploitability is not established in the current artifact.

Recommended fix:
- Use Jackson's ObjectMapper to serialize objects.
- Validate role values against an explicit allowlist.
- Never construct JSON through string concatenation.

Finding D8 — Repository uses an unsafe shared in-memory data structure

Location: AdminUserRepository.java

private final Map<String, String> profiles = new HashMap<>();

Defect class: Shared mutable state / concurrency and persistence weakness.

Risk:

The repository is singleton-scoped.

Its HashMap is consequently shared across requests.

Concurrent reads and writes are not protected by synchronization or a concurrent collection.

This can cause inconsistent behavior under concurrent access.

Additionally, all profile information disappears when the application restarts.

Recommended fix:
- Use a persistent database-backed repository.
- Use appropriate transaction management.
- Avoid sharing unsynchronized mutable collections across concurrent requests.

Finding D9 — Audit service does not implement its claimed security function

Location: InternalAuditService.java

public void record(String actor, String action) {
    
    // Appends a tamper-evident entry to the internal audit trail.

}

public void purgeTrail(String userId) {
    
    // Privileged: removes audit-trail entries for a user.

}

public String exportTrail() {
    
    return "";

}

Defect class: Missing security logging / inadequate audit integrity.

Risk:

The implementation contains comments describing security functionality, but the supplied methods do not actually implement it.

record() does not record anything.

Consequently:
- Profile access cannot be reliably audited.
- Suspicious activity cannot be reconstructed from this service.
- There is no demonstrated tamper-evident audit trail.
Additionally, the service exposes privileged purge and export operations through the same bean.

Recommended fix:
- Implement persistent audit storage.
- Make audit entries append-only.
- Restrict deletion and export operations to dedicated administrative services.
- Protect audit records against unauthorized modification.
- Record authenticated actor identity rather than trusting caller-supplied identity.

3. Cross-Layer Reconciliation

This is where the configuration and DI passes reveal problems that might be missed when reviewing the files independently.

| Cross-layer finding | Configuration location | DI/code location | Combined weakness |
|---|---|---|---|
| X1 — JWT secret exposure and weak signing | `application.yml`: `portal.jwt.secret` | `JwtTokenService.java` | Hardcoded secret combined with noncryptographic token generation. |
| X2 — Uncontrolled implementation selection | `application.yml`: `portal.notifier.class` | `NotifierConfig.java` | Externally overridable class name passed into reflective construction. |
| X3 — Public Actuator access | `application.yml`: `management.endpoints.web.exposure.include` | `SecurityConfig.java` | All management endpoints exposed and no authentication required. |
| X4 — Remote application shutdown | `application.yml`: `management.endpoint.shutdown.enabled` | `SecurityConfig.java` | Shutdown endpoint enabled, exposed, and accessible without authentication. |
| X5 — HTTPS not enforced | `application.yml`: `spring.security.require-ssl` | `SecurityConfig.java` | Invalid SSL property and no alternative HTTPS enforcement. |
| X6 — Cross-user identity leakage | No applicable corrective configuration | `CurrentUserService.java` + `ProfileController.java` | Shared singleton state allows concurrent requests to overwrite each other's identity. |


Important distinction:

X1–X5 are configuration/wiring intersections.

X6 is a scope and data-flow intersection: the controller uses a singleton state holder as though it were request-specific.

These should not be counted as unrelated vulnerabilities simply because they appear in multiple files.

4. Complete Dependency Graph

The following represents the application-defined dependency relationships.

                  PortalApplication
                         |
                  Component Scan
                         |
          +--------------+---------------+
          |                              |
     SecurityConfig                 NotifierConfig
          |                              |
          v                              v
  SecurityFilterChain                  Notifier
          |                              |
          |                         EmailNotifier
          |
          v
     Security Rules
          |
       permitAll


              ProfileController
                      |
       +--------------+--------------+
       |              |              |
       v              v              v
 AdminUserRepository  CurrentUserService  InternalAuditService
       |              |              |
       |              |              |
       v              v              v
   HashMap         Shared userId    Audit operations
       |
       |
       +---- updateProfile()
       +---- deleteUser()
       +---- grantRole()


             JwtTokenService
                    |
              @Value injection
                    |
          +---------+---------+
          |                   |
          v                   v
      JWT Secret           JWT TTL
      (Config)             (Config)                 

Scope assessment
| Bean | Scope | Assessment |
|---|---|---|
| PortalApplication | Singleton | Appropriate |
| SecurityConfig | Singleton | Appropriate |
| NotifierConfig | Singleton | Appropriate |
| SecurityFilterChain | Singleton | Appropriate |
| Notifier | Singleton | Appropriate for the supplied stateless implementation |
| AdminUserRepository | Singleton | Problematic mutable shared state |
| CurrentUserService | Singleton | Critical scope mismatch |
| InternalAuditService | Singleton | Appropriate if implemented statelessly |
| JwtTokenService | Singleton | Appropriate for immutable configuration |
| ProfileController | Singleton | Normal Spring controller scope |


The most important scope defect is CurrentUserService.

The repository also carries shared mutable state, but its problem is primarily its unsynchronized in-memory storage design.

5. Coverage Check

I deliberately reviewed the major vulnerability categories rather than assuming that the identified findings represented the entire audit.

| Vulnerability category | Result |
|---|---|
| Hardcoded secrets | Finding C1, C2 |
| Weak cryptography | Finding D4 |
| Invalid framework properties | Finding C3 |
| Insecure network binding | Finding C7 |
| Missing HTTPS enforcement | Finding C3 |
| Excessive information disclosure | Finding C4, C5 |
| Exposed administrative endpoints | Finding C5, C6 |
| Missing authentication | Finding D1 |
| Missing authorization | Finding D1 |
| CSRF protection | Finding D2 |
| Unsafe reflection | Finding D5 |
| Bean provenance | Finding D5, cross-layer X1–X5 |
| Incorrect bean scope | Finding D3 |
| Shared mutable state | Finding D3, D8 |
| Excessive dependency capabilities | Finding D6, D9 |
| Unsafe input handling | Finding D7 |
| Audit integrity | Finding D9 |
| Dependency maintenance | Finding C8 |
| SQL injection | No evidence identified |
| CORS misconfiguration | No explicit CORS configuration identified |
| Unsafe deserialization | No evidence identified |
| XSS | No demonstrated browser-rendering or HTML injection path in supplied code |


The last four categories are not positive security findings. They were checked, but the supplied artifact does not establish those vulnerabilities.

6. Final Assessment

| Severity | Findings |
|---|---|
| **Critical** | D1, C6/X4 — No authentication and remotely accessible shutdown functionality |
| **High** | C1, C2/D4, C3, C4, C5/X3, D3, D5/X2, D6, D9 |
| **Medium** | C7, C8, D2, D7, D8 |


Severity reflects the potential impact and exposure in the supplied implementation; actual exploitability may depend on deployment network controls and whether the in-memory repository is populated.

Deployment decision: REJECT

I would not approve this application for production deployment.

The five most important remediation priorities are:

1. Implement authentication and authorization. Replace permitAll() with authenticated access and appropriate roles.
2. Secure Actuator. Remove wildcard exposure, disable shutdown, and protect management endpoints.
3. Fix shared singleton state. Remove the mutable userId field from CurrentUserService.
4. Replace the JWT implementation. Use genuine cryptographic signing and verification with securely managed keys.
5. Remove uncontrolled reflection. Replace configuration-driven arbitrary class loading with explicitly registered implementations.

After those changes, address the remaining findings, upgrade Spring Boot, and perform a fresh security review.

Bottom line: This application demonstrates several classic AI-generated code risks: plausible-looking but ineffective security configuration, unsafe dependency wiring, inappropriate singleton state, and security functionality that exists in comments rather than implementation.

A successful build and single-user smoke test provide no meaningful assurance against these vulnerabilities.

Framework verification references
- Spring Boot 3.2.5 Reference Documentation — verifies Actuator exposure and security behavior.
- Spring Boot Actuator Security Documentation — explains how a custom SecurityFilterChain changes Actuator security auto-configuration.
- Spring Boot 3.2.12 Release Announcement — confirms the end of open-source support for Spring Boot 3.2.x.

Overall audit result: Multiple independently exploitable weaknesses and several cross-layer vulnerabilities. 

Not production-ready.