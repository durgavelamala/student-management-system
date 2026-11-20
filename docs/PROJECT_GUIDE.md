# Project Guide – Concepts and Viva Preparation

This guide explains the DevOps concepts used in the Student Management System and gives you a ready explanation for the project demonstration and viva.

---

## Part A – Maven

### What is Maven?
Maven is a **build automation and dependency management tool** for Java. Instead of downloading JAR libraries by hand and compiling with `javac`, we describe the project in `pom.xml` and Maven downloads libraries, compiles, tests and packages the application in a standard way.

### Maven project structure (convention over configuration)
Maven expects a fixed folder layout, so no extra configuration is needed:

| Folder | Contents |
|---|---|
| `src/main/java` | application source code |
| `src/main/resources` | `application.properties`, Thymeleaf templates, CSS, JS |
| `src/test/java` | test classes |
| `src/test/resources` | test configuration (H2 database settings) |
| `target/` | output created by Maven (compiled classes, test reports, the JAR) |

### `pom.xml` (Project Object Model)
The heart of a Maven project. In our file:

- **`<parent>` spring-boot-starter-parent** – provides tested versions of all Spring libraries and default plugin settings, so we do not write version numbers for each dependency.
- **Coordinates** – `groupId` (`com.college`), `artifactId` (`student-management-system`), `version` (`1.0.0`) uniquely identify the project.
- **`<properties>`** – `java.version = 17`.
- **`<dependencies>`** – the libraries we need (see below).
- **`<build>`** – `finalName` fixes the JAR name to `student-management-system.jar` (the Dockerfile copies this file), and the plugins.

### Dependencies
A dependency is an external library. Maven downloads it (and the libraries it needs – *transitive dependencies*) from Maven Central into `~/.m2/repository`.

| Dependency | Why | Scope |
|---|---|---|
| spring-boot-starter-web | Spring MVC + embedded Tomcat server | compile |
| spring-boot-starter-thymeleaf | HTML templates | compile |
| spring-boot-starter-security | login, logout, roles | compile |
| thymeleaf-extras-springsecurity6 | show the logged-in user in pages | compile |
| spring-boot-starter-data-jpa | Hibernate + repositories | compile |
| spring-boot-starter-validation | `@NotBlank`, `@Email`, … | compile |
| mysql-connector-j | MySQL JDBC driver | **runtime** – needed only when the app runs |
| h2 | in-memory database for tests | **test** – not inside the final JAR |
| spring-boot-starter-test | JUnit 5, Mockito, AssertJ, MockMvc | **test** |
| spring-security-test | log in as a fake user in tests | **test** |

### Maven plugins
Plugins do the actual work in each phase:
- **maven-compiler-plugin** – compiles Java (configured by the parent POM for Java 17).
- **maven-surefire-plugin** – runs the tests in `mvn test`, writes XML reports to `target/surefire-reports` (Jenkins reads these).
- **maven-jar-plugin** – creates the JAR.
- **spring-boot-maven-plugin** – repackages the JAR into an *executable fat JAR* containing all dependencies and an embedded Tomcat, so it runs with `java -jar`. Also provides `mvn spring-boot:run`.

### Maven lifecycle
The default lifecycle is an ordered list of phases. Running a phase runs **all phases before it**:

```
validate → compile → test → package → verify → install → deploy
```

`clean` belongs to a separate *clean* lifecycle.

| Command | What happens in this project |
|---|---|
| `mvn clean` | deletes `target/` so the next build starts fresh |
| `mvn compile` | validate + compile → `target/classes` |
| `mvn test` | … + compiles tests and runs 4 test classes (H2 database, no MySQL needed) |
| `mvn package` | … + builds `target/student-management-system.jar` |
| `mvn clean package` | clean, then everything up to package – used for the final build |

---

## Part B – Testing

| Test class | Type | What it checks |
|---|---|---|
| `MarksTest` | plain unit test | grade boundaries (A+, A, B, …), pass mark, initials |
| `StudentServiceTest` | unit test with **Mockito** mocks | duplicate student ID is rejected; a new student gets a login account with an encoded password; blank search returns all |
| `AttendanceServiceTest` | unit test | percentage calculation and rounding; future dates are rejected |
| `WebApplicationTest` | integration test (**@SpringBootTest + MockMvc**, H2 DB) | login redirects by role, wrong password, students blocked from teacher pages (403), every page renders, add/validate/delete student |

Why H2 for tests? Jenkins can run `mvn test` without installing MySQL, and each test run starts with a fresh database.

---

## Part C – Jenkins

### What is Jenkins?
Jenkins is an open-source **automation server** used for **Continuous Integration (CI)**: every time code changes, it automatically fetches the code, builds it, tests it and packages it, so problems are found early.

### Pipeline as code
The pipeline is written in the **`Jenkinsfile`** stored in the project root (a *Declarative Pipeline*). Because it is in Git, the build process is versioned together with the code.

Key parts of our Jenkinsfile:
- `agent any` – run on any available Jenkins machine.
- `tools { jdk 'JDK17'; maven 'Maven3' }` – use the JDK and Maven configured in *Manage Jenkins → Tools*.
- `environment` – image name and tag (tag = Jenkins build number).
- `stages` – the steps below.
- `post` – actions after a stage or the whole pipeline (publish test results, archive the JAR, print success/failure).
- `runCmd()` – uses `sh` on Linux and `bat` on Windows so the same Jenkinsfile works on both.

### Stages

| # | Stage | Command | Purpose |
|---|---|---|---|
| 1 | **Checkout** | `checkout scm` | Downloads the code from the GitHub repository configured in the job |
| 2 | **Build** | `mvn -B clean compile` | Checks that the code compiles (`-B` = batch mode, cleaner logs) |
| 3 | **Test** | `mvn -B test` | Runs all tests; `junit` publishes results on the build page. A failing test stops the pipeline |
| 4 | **Package** | `mvn -B package -DskipTests` | Creates the executable JAR (tests already passed); `archiveArtifacts` stores it in Jenkins |
| 5 | **Docker Build** | `docker build -t student-management-system:<build> -t …:latest .` | Builds the Docker image from the JAR – only reached when tests passed |

---

## Part D – Docker

### What is Docker?
Docker packages an application together with everything it needs (here: the Java 17 runtime and the JAR) into an **image**. A running image is a **container**. The app then runs the same way on any machine with Docker – "it works on my machine" problems disappear.

| Term | In this project |
|---|---|
| **Dockerfile** | recipe for our image: start `FROM eclipse-temurin:17-jre`, `COPY` the Maven JAR, `EXPOSE 8080`, `ENTRYPOINT java -jar app.jar` |
| **Image** | `student-management-system:latest` (and one tag per Jenkins build) |
| **Container** | `sms-app` (our app) and `sms-mysql` (database) |
| **.dockerignore** | sends only the JAR to the Docker build (faster, no source code or secrets in the image) |
| **Base image** | `eclipse-temurin:17-jre` – official Java 17 runtime; `mysql:8.0` – official MySQL |
| **Port mapping** | `8081:8080` app (8081 on the PC avoids Jenkins on 8080), `3307:3306` MySQL (host:container) |
| **Volume** | `mysql_data` keeps the database files, so data survives container restarts and `docker compose down` |
| **Environment variables** | DB host, name, user, password passed to the containers (read by `application.properties`) |

### Docker Compose
`docker-compose.yml` describes **multi-container** applications. One command, `docker compose up -d --build`, creates a private network, starts MySQL, waits until its **healthcheck** passes (`depends_on: condition: service_healthy`), then starts the app.

Inside the Compose network, containers reach each other by **service name**: the app uses `DB_HOST=mysql`, not `localhost` (inside a container, `localhost` means the container itself).

---

## Part E – Mini-project explanation for the viva

**Title:** Student Management System with CI/CD using Maven, Jenkins and Docker.

**Problem:** Colleges manage student details, attendance, marks and notices on paper or spreadsheets, which is slow, error-prone and gives students no direct access to their own records.

**Solution:** A web application with two roles.
Teachers manage students (add, search, edit, delete), mark attendance per subject and date, enter marks, and publish announcements. Students log in to see only their own profile, attendance percentage, marks, grades and announcements.

**How it is built:**
1. Spring Boot application in a layered architecture: controllers handle web requests, services contain business rules (grades, percentages, validation of duplicates), repositories access MySQL using Spring Data JPA, entities map to tables.
2. Spring Security authenticates users from the `users` table with BCrypt passwords and authorises URLs by role: `/teacher/**` for TEACHER, `/student/**` for STUDENT. After login a custom success handler redirects to the correct dashboard. Security is enforced on the server, not with JavaScript.
3. Thymeleaf renders the HTML pages; CSS gives a responsive blue/white design and Chart.js draws the dashboard charts.

**DevOps flow:** The code is on GitHub. Jenkins pulls it, Maven compiles it, runs 4 test classes (unit tests with Mockito and integration tests with an H2 database), packages an executable JAR, and Docker builds an image from the JAR. Docker Compose runs the image together with a MySQL container and a persistent volume, so the whole system starts with one command.

**Result:** A working, tested, containerised application whose build is fully automated; any code change can be built, tested and turned into a new image by clicking "Build Now" (or automatically through polling).

**Possible future enhancements:** password change and reset, multiple teachers per subject, exam types (internal/external), CSV/PDF reports, pushing images to Docker Hub, automatic deployment stage in Jenkins.

### Suggested demo order (≈ 10 minutes)
1. Show the GitHub repository and the `Jenkinsfile`.
2. Click **Build Now** in Jenkins; walk through the 5 stages and the test report.
3. Run `docker images` to show the new image, then `docker compose up -d` and `docker ps`.
4. Open the app → wrong password (error message) → log in as **teacher**: dashboard, search a student, add a student (show validation), mark attendance, enter marks, post an announcement.
5. Log out → log in as **student** (or the new student `stu007` / `student123`): dashboard, marks, attendance, the announcement just posted.
6. While logged in as student, type `/teacher/dashboard` in the address bar → *Access denied*.
7. `docker compose down` then `docker compose up -d` → data is still there (volume).

### Common viva questions

| Question | Short answer |
|---|---|
| Why Maven? | Standard structure, automatic dependency download, one command to compile/test/package, works the same in Jenkins |
| What is `pom.xml`? | Project Object Model – coordinates, dependencies, plugins and build settings of the project |
| Difference between `mvn package` and `mvn install`? | `package` creates the JAR in `target/`; `install` also copies it into the local `~/.m2` repository |
| What is dependency scope? | Where a library is used: compile (always), runtime (only when running, e.g. MySQL driver), test (only for tests, e.g. H2, JUnit) |
| What is CI? | Automatically building and testing every change so errors are found early |
| CI vs CD? | CI = build + test automatically; CD = also deliver/deploy the result automatically. Our pipeline does CI and produces a deployable Docker image |
| Why a Jenkinsfile instead of configuring the job in the UI? | Pipeline as code: versioned in Git, reviewable, reproducible |
| What happens if a test fails? | The Test stage fails, later stages (Package, Docker Build) are skipped, the build is red |
| Image vs container? | Image = read-only template; container = running instance of an image |
| Why is the database not `localhost` in Docker? | Each container has its own `localhost`; containers talk via the Compose service name `mysql` |
| How does data persist? | MySQL files are stored in the named volume `mysql_data`, which is not deleted with the container |
| How are passwords stored? | BCrypt hash, never plain text |
| How do you stop a student from opening teacher pages? | Spring Security URL rules (`hasRole("TEACHER")`), server-side; returns 403 |
| How does a student see only their own data? | The student controller looks up the student by the logged-in username – no student ID is taken from the URL |
| What is CSRF and is it handled? | Cross-Site Request Forgery; Spring Security enables CSRF protection and Thymeleaf adds the token to every `th:action` form automatically |
| Why H2 in tests? | Tests run anywhere (Jenkins) without a MySQL server and start from a clean database |
| What is `ddl-auto=update`? | Hibernate creates/updates tables from the entities at start-up and keeps existing data |

---

## Part F – DevOps concepts demonstrated and where

| Concept | Where in the project |
|---|---|
| Version control | Git repository on GitHub; `.gitignore` excludes `target/` and `.env` |
| Build automation | `pom.xml`, `mvn clean package` |
| Dependency management | `<dependencies>` in `pom.xml`, scopes runtime/test |
| Build lifecycle | clean → compile → test → package (Jenkins stages 2-4) |
| Automated testing | `src/test/java` – JUnit 5, Mockito, MockMvc; Surefire reports |
| Continuous Integration | `Jenkinsfile`, Jenkins Pipeline job connected to GitHub |
| Pipeline as code | `Jenkinsfile` in the repository root |
| Quality gate | Docker Build stage runs only after tests pass |
| Build artifacts | `archiveArtifacts` saves the JAR in Jenkins |
| Containerisation | `Dockerfile`, image built from the Maven JAR |
| Image tagging / versioning | `student-management-system:<BUILD_NUMBER>` and `:latest` |
| Multi-container orchestration | `docker-compose.yml` (app + MySQL) |
| Service discovery | app connects to `mysql` service name |
| Health checks & start order | MySQL `healthcheck` + `depends_on: service_healthy` |
| Persistent storage | `mysql_data` Docker volume |
| Externalised configuration | `${DB_HOST:localhost}` etc. in `application.properties`; `.env` for Compose |
| Secrets kept out of Git/images | `.env` in `.gitignore`, `.dockerignore` allows only the JAR |
| Environment parity | same image runs on any machine with Docker |
