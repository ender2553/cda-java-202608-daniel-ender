1.          Dependency Graph        
                  
                  
                  ReportingConfig
                   (@Configuration)
                         |
              +----------+----------+
              |                     |
              v                     v
       DataExporter              AuditLog
        (@Bean)                   (@Bean)
              |                     |
       CSV or Remote             NoOpAuditLog
         (selected)              (selected)
              |                     |
              +----------+----------+
                         |
                         v
                   ReportService
                     (@Service)
                         ^
                         |
                         |
                  ReportController
                  (@RestController)
                         |
                         v
                   RequestContext
                   (@Component)



-Additional injection relationships:

ReportService ------> DataExporter
ReportService ------> AuditLog
ReportService ------> RequestContext

ReportController --> ReportService
ReportController --> RequestContext

2. Bean Inventory and Annotations
There are 7 Spring-managed beans.     
     
     | Bean | Registration | Concrete implementation |
|---|---|---|
| ReportApplication | `@SpringBootApplication` | ReportApplication |
| ReportController | `@RestController` | ReportController |
| ReportService | `@Service` | ReportService |
| RequestContext | `@Component` | RequestContext |
| ReportingConfig | `@Configuration` | ReportingConfig |
| DataExporter | `@Bean` | CsvExporter or RemoteScriptExporter |
| AuditLog | `@Bean` | NoOpAuditLog |       
            
3. Node Annotations
A. ReportApplication
- What: Spring Boot application entry point.
- Scope: Singleton.
- State: No per-user state.
- Source: Fixed application configuration through @SpringBootApplication.
- Finding: No obvious concern.
B. ReportController
- What: ReportController.
- Scope: Singleton (default Spring scope).
- State: Holds references to ReportService and RequestContext.
- Source: Component scanning through @RestController.
- Finding: Potential concern because it injects a shared request-context bean.
C. ReportService
- What: ReportService.
- Scope: Singleton.
- State: Holds references to exporter, audit logger, and request context.
- Source: Component scanning through @Service.
- Finding: Potential concern because it depends on shared per-user state.
D. RequestContext 🔴 SUSPECT
- What: RequestContext.
- Scope: Singleton (default @Component scope).
- State: Holds mutable currentUser field.
- Source: Component scanning through @Component.
- Finding: Suspicious scope. Per-user request information is stored in a singleton bean.
E. ReportingConfig
- What: ReportingConfig.
- Scope: Singleton.
- State: Configuration/factory methods.
- Source: Fixed @Configuration class.
- Finding: No obvious concern by itself.
F. DataExporter 🔴 SUSPECT
- What: Interface binding to:
  - CsvExporter (default).
  - RemoteScriptExporter (when configured as remote).
- Scope: Singleton by default because it is created through @Bean.
- State: Neither implementation shows mutable instance state.
- Source: Selected dynamically through report.exporter in application.yml.
Current configuration:
report:
  exporter: csv

Finding: Implementation selection is configuration-dependent rather than fixed. The remote implementation is potentially unexpected and deserves review.
G. AuditLog
- What: Interface binding to NoOpAuditLog.
- Scope: Singleton.
- State: No meaningful mutable instance state.
- Source: Fixed binding in ReportingConfig.auditLog().
- Finding: Suspicious implementation choice. NoOpAuditLog silently discards audit events instead of recording them.            
            
4.Suspect Nodes

| Node | Concern | Reason |
|---|---|---|
| 🔴 RequestContext | Scope/state mismatch | Singleton holds mutable per-user information. |
| 🔴 DataExporter | Dynamic implementation | Configuration determines which implementation is injected. |
| 🟠 AuditLog | Unexpected implementation | NoOpAuditLog does not actually record events. |
| 🟠 ReportService | Shared dependency | Injects the singleton RequestContext. |
| 🟠 ReportController | Shared dependency | Also injects the singleton RequestContext. |

Important observations
RequestContext
- User-specific information is mutable.
- Bean uses default singleton scope.
- Multiple requests share the same instance.
DataExporter
- Interface binding is not fixed.
- Configuration controls implementation selection.
- RemoteScriptExporter is an alternative implementation.
AuditLog
- Interface suggests audit recording.
- Actual implementation discards all events.
- Binding is fixed in code, not selected from configuration.

5.Broadest Blast Radius
Winner: RequestContext

It has the most incoming injection edges.
Injecting bean	Dependency
ReportController	RequestContext
ReportService	RequestContext


Total incoming injection edges: 2

It is particularly important because both the controller and service depend on the same singleton instance.

For comparison:
Bean	Incoming injection edges
RequestContext	    2
ReportService	    1
DataExporter	    1
AuditLog	        1
ReportController	0
ReportingConfig	    0
ReportApplication	0


6.Final Condensed Audit Findings
    1. RequestContext — HIGH CONCERN: Singleton contains mutable per-user state. Its scope does not match the lifetime of the data it stores.
    2. DataExporter — MEDIUM CONCERN: Interface implementation is selected dynamically through configuration. RemoteScriptExporter is an alternative implementation.
    3. AuditLog — MEDIUM CONCERN: Fixed binding uses NoOpAuditLog, which silently discards audit events.
    4. ReportService — REVIEW: Singleton depends on shared mutable RequestContext.
    5. ReportController — REVIEW: Singleton also depends on shared mutable RequestContext.

Bottom Line
The most significant dependency-injection concern is RequestContext because its singleton scope conflicts with its per-user state.

DataExporter is the next important review target because its concrete implementation depends on configuration.

RequestContext has the broadest blast radius, with two incoming injection edges.