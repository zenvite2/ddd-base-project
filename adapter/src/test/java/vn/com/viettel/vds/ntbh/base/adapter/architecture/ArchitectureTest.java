package vn.com.viettel.vds.ntbh.base.adapter.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

// TODO: Remove this sample code when implementing the actual service
// ArchUnit test enforces all dependency rules of Clean Architecture
// Reference: service-architecture.md § Summary of rules
@AnalyzeClasses(
    packages = "vn.com.viettel.vds.ntbh.base",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  // ═══════════════════════════════════════════════════════════════
  // 1. Dependency rules between layers (Clean Architecture)
  //    domain ← application ← adapter (inner layers do not import outer layers)
  // ═══════════════════════════════════════════════════════════════

  @ArchTest
  static final ArchRule domain_should_not_depend_on_application =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..application..")
          .because(
              "Domain layer must not depend on Application layer"
                  + " — violates Clean Architecture dependency rule");

  @ArchTest
  static final ArchRule domain_should_not_depend_on_adapter =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter..")
          .because(
              "Domain layer must not depend on Adapter layer"
                  + " — violates Clean Architecture dependency rule");

  @ArchTest
  static final ArchRule application_should_not_depend_on_adapter =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter..")
          .because(
              "Application layer must not depend on Adapter layer"
                  + " — violates Clean Architecture dependency rule");

  // ═══════════════════════════════════════════════════════════════
  // 2. Domain purity — pure Java, no frameworks
  //    Ref: service-architecture.md § Domain Layer
  //    "No Spring, JPA, or any framework imports"
  // ═══════════════════════════════════════════════════════════════

  @ArchTest
  static final ArchRule domain_should_be_pure_java =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..")
          .because(
              "Domain layer must be pure Java — no Spring, JPA, or Hibernate imports allowed."
                  + " May only depend on vds-platform:domain-core and standard Java libraries");

  // ═══════════════════════════════════════════════════════════════
  // 3. UseCase rules
  //    Ref: service-architecture.md § Application Layer
  //    "Use case is not a Spring bean — instantiated via @Configuration"
  //    "Use case only uses domain model — no JPA entity imports"
  // ═══════════════════════════════════════════════════════════════

  @ArchTest
  static final ArchRule usecase_should_not_be_spring_bean =
      classes()
          .that()
          .resideInAPackage("..application.usecase..")
          .should()
          .notBeAnnotatedWith("org.springframework.stereotype.Service")
          .andShould()
          .notBeAnnotatedWith("org.springframework.stereotype.Component")
          .andShould()
          .notBeAnnotatedWith("org.springframework.stereotype.Repository")
          .because(
              "UseCase is not a Spring bean — must be instantiated via @Configuration @Bean in Adapter layer."
                  + " Using @Service/@Component bypasses lifecycle control");

  @ArchTest
  static final ArchRule usecase_should_not_import_jpa_entity =
      noClasses()
          .that()
          .resideInAPackage("..application.usecase..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter.outbound.persistence.jpa..")
          .because(
              "UseCase must only use domain model — no JPA entity imports allowed."
                  + " Must call domain Repository interface, not JPA repository directly");

  // ═══════════════════════════════════════════════════════════════
  // 4. Inbound/Outbound separation in Adapter
  //    Ref: service-architecture.md § Adapter Layer
  //    Inbound (controller, scheduler, facade) ≠ Outbound (JPA, client, port impl)
  // ═══════════════════════════════════════════════════════════════

  @ArchTest
  static final ArchRule inbound_should_not_access_outbound =
      noClasses()
          .that()
          .resideInAPackage("..adapter.inbound..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter.outbound..")
          .because(
              "Inbound adapter (Controller, Scheduler) must not directly access Outbound adapter (JPA, Kafka, Client)."
                  + " Must go through Facade → Port → UseCase → Repository");

  // ═══════════════════════════════════════════════════════════════
  // 5. Controller/Scheduler must go through Facade
  //    Ref: service-architecture.md § Summary of rules
  //    "Controller: Receives request, calls facade, returns response.
  //     Does not call use case directly, contains no logic"
  // ═══════════════════════════════════════════════════════════════

  @ArchTest
  static final ArchRule controller_should_go_through_facade =
      noClasses()
          .that()
          .resideInAPackage("..adapter.inbound.restful.controller..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..application..")
          .because(
              "Controller must not call UseCase/Port directly — must go through Facade."
                  + " Facade is responsible for mapping DTOs and delegating to Port");

  @ArchTest
  static final ArchRule scheduler_should_go_through_facade =
      noClasses()
          .that()
          .resideInAPackage("..adapter.inbound.scheduler..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..application..")
          .because(
              "Scheduler must not call UseCase/Port directly — must go through Facade."
                  + " Keeps consistent flow: Scheduler → Facade → Port → UseCase");

  // ═══════════════════════════════════════════════════════════════
  // 6. Facade rules
  //    Ref: service-architecture.md § Facade Example
  //    "Facade only orchestrates: map DTO → call port → map response"
  //    "❌ Wrong: calling JPA repo directly"
  // ═══════════════════════════════════════════════════════════════

  @ArchTest
  static final ArchRule facade_should_not_access_jpa =
      noClasses()
          .that()
          .resideInAPackage("..adapter.inbound.facade..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter.outbound.persistence.jpa..")
          .because(
              "Facade must not call JPA repository directly."
                  + " Must go through Port → UseCase → domain Repository");

  // ═══════════════════════════════════════════════════════════════
  // 7. Temporal SDK must stay in Adapter layer
  //    Workflow/Activity interfaces in application/ are plain Java
  //    Temporal annotations (@WorkflowInterface, @ActivityInterface) live in adapter/
  // ═══════════════════════════════════════════════════════════════

  @ArchTest
  static final ArchRule application_should_not_import_temporal =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("io.temporal..")
          .because(
              "Application layer must not import Temporal SDK."
                  + " Workflow/Activity interfaces in application/ must be plain Java."
                  + " Temporal annotations live in adapter/inbound/temporal/");

  @ArchTest
  static final ArchRule domain_should_not_import_temporal =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("io.temporal..")
          .because(
              "Domain layer must not import Temporal SDK — must remain pure Java");

  @ArchTest
  static final ArchRule facade_should_not_access_outbound =
      noClasses()
          .that()
          .resideInAPackage("..adapter.inbound.facade..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter.outbound..")
          .because(
              "Facade must not depend on Outbound adapter (persistence, client)."
                  + " Facade only maps DTOs and delegates to Application Port, contains no business logic");
}
