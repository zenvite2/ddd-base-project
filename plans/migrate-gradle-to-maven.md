# Plan: Chuyển từ Gradle sang Maven + Upgrade Spring Boot 3.5.13

## Tổng quan

Chuyển build system từ Gradle Kotlin DSL sang Maven, đồng thời nâng Spring Boot từ 3.4.9 lên 3.5.13.

## Hiện trạng

| Thành phần | Hiện tại |
|---|---|
| Build tool | Gradle 8.14 + Kotlin DSL |
| Spring Boot | 3.4.9 |
| Java | 21 |
| Modules | root → domain, application, adapter |
| Nexus repo | http://nexus.digital.vn (user: dev) |
| Code formatter | Spotless + Google Java Format |

## Mục tiêu

| Thành phần | Sau chuyển đổi |
|---|---|
| Build tool | Maven 3.9.x (Maven Wrapper) |
| Spring Boot | 3.5.13 |
| Java | 21 (giữ nguyên) |
| Modules | Giữ nguyên 3 modules: domain, application, adapter |

## Các bước thực hiện

### Bước 1: Tạo Maven POM files

1. **Root `pom.xml`** — parent POM:
   - Parent: `spring-boot-starter-parent:3.5.13`
   - groupId: `vn.com.viettel.vds.ntbh`
   - artifactId: `ntbh-base-project`
   - packaging: `pom`
   - Modules: domain, application, adapter
   - Properties: Java 21, UTF-8 encoding, dependency versions (MapStruct 1.6.3, ArchUnit 1.3.0, RestAssured 5.5.7)
   - dependencyManagement: BOM cho JUnit, vds-platform libs
   - Repository: VDS Nexus + Maven Central
   - Plugin management: maven-compiler-plugin (Java 21 + MapStruct + Lombok), spotless-maven-plugin (Google Java Format)

2. **`domain/pom.xml`**:
   - Parent: root POM
   - Dependencies: `vn.com.viettel.vds:domain-core:1.0.0-SNAPSHOT`
   - Test: JUnit Jupiter, AssertJ

3. **`application/pom.xml`**:
   - Parent: root POM
   - Dependencies: domain module, `vn.com.viettel.vds:event-core:1.0.0-SNAPSHOT`
   - Test: JUnit, AssertJ, Mockito

4. **`adapter/pom.xml`**:
   - Parent: root POM
   - Dependencies: application module, domain module, spring-web (vds), Spring Boot starters, MariaDB, Lombok, MapStruct, Kafka, Redis, Test dependencies
   - Plugin: spring-boot-maven-plugin
   - Spring Boot repackage

### Bước 2: Thêm Maven Wrapper

- Chạy `mvn wrapper:wrapper` hoặc tạo thủ công `.mvn/wrapper/maven-wrapper.properties`
- Tạo `mvnw` và `mvnw.cmd`

### Bước 3: Cập nhật `.gitignore`

- Bỏ các entry Gradle (`build/`, `.gradle/`, `!gradle/wrapper/gradle-wrapper.jar`)
- Thêm các entry Maven (`target/`, `.mvn/wrapper/maven-wrapper.jar`)

### Bước 4: Xóa các file Gradle

- `build.gradle.kts` (root + 3 modules)
- `settings.gradle.kts`
- `gradle.properties`
- `gradle/` directory (wrapper)
- `gradlew`, `gradlew.bat`
- `.gradle/` directory (cache)

### Bước 5: Cập nhật CLAUDE.md

- Cập nhật build commands trong CLAUDE.md (Gradle → Maven)

### Bước 6: Verify build

- `./mvnw clean install -DskipTests`
- `./mvnw test -pl domain`
- `./mvnw test -pl application`

## Mapping Gradle → Maven Dependencies

| Gradle | Maven |
|---|---|
| `api(...)` | `<scope>compile</scope>` (default) |
| `implementation(...)` | `<scope>compile</scope>` (default) |
| `runtimeOnly(...)` | `<scope>runtime</scope>` |
| `compileOnly(...)` | `<scope>provided</scope>` |
| `annotationProcessor(...)` | `maven-compiler-plugin` annotationProcessorPaths |
| `testImplementation(...)` | `<scope>test</scope>` |

## Lưu ý

- `gradle.properties` chứa credentials Nexus → chuyển sang `settings.xml` (~/.m2/settings.xml) hoặc trong POM sử dụng property với giá trị default
- Spotless Gradle plugin → chuyển sang `spotless-maven-plugin` (cùng tác giả Diffplug)
- Spring dependency management plugin không cần nữa vì dùng `spring-boot-starter-parent` làm parent POM
- `tasks.named<Jar>("jar") { enabled = false }` trong adapter → tương đương với `spring-boot-maven-plugin` repackage (mặc định)
