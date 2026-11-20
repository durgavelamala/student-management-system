# Student Management System – DevOps Mini Project

A web-based Student Management System built with **Java 17 + Spring Boot**, with login and role-based dashboards for **Teachers** and **Students**.
The project demonstrates a complete DevOps flow: **Git/GitHub → Jenkins → Maven (build + tests) → Docker image → Docker Compose (app + MySQL)**.

---

## 1. Features

| Teacher | Student |
|---|---|
| Dashboard with totals, averages and charts | Dashboard with own attendance, marks and chart |
| View, search, add, edit, delete, view-details of students | View own profile (read-only) |
| Mark attendance (present/absent) per subject and date | View own subject-wise attendance + full record list |
| View attendance percentage of every student | View own marks, grades, pass/fail |
| Add and update marks per subject | Read announcements |
| Create and delete announcements | |

Other features: Spring Security login with BCrypt passwords, show/hide password, error message on wrong credentials, logout, role-based redirection, form validation with messages, success/error alerts, responsive design (sidebar collapses on mobile), demo data on first start.

## 2. Technology Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3 (Spring MVC, Spring Security, Spring Data JPA / Hibernate, Validation) |
| UI | Thymeleaf, HTML, CSS, JavaScript, Bootstrap Icons, Chart.js |
| Database | MySQL 8 (H2 in-memory only for automated tests) |
| Build | Maven |
| CI | Jenkins (Declarative Pipeline, `Jenkinsfile`) |
| Containers | Docker, Docker Compose |
| Version control | Git, GitHub |

## 3. Project Architecture

Simple **monolithic, layered** architecture (MVC):

```
 Browser (Thymeleaf HTML + CSS + JS)
        │  HTTP
        ▼
 ┌─────────────────────────────────────────────┐
 │ Spring Security filter chain                │  login, logout, /teacher/** vs /student/**
 ├─────────────────────────────────────────────┤
 │ Controller layer   (controller package)     │  handles URLs, validates forms, picks the page
 ├─────────────────────────────────────────────┤
 │ Service layer      (service package)        │  business rules (grades, percentages, CRUD rules)
 ├─────────────────────────────────────────────┤
 │ Repository layer   (repository package)     │  Spring Data JPA interfaces
 ├─────────────────────────────────────────────┤
 │ Entity layer       (entity package)         │  Java classes mapped to tables (Hibernate)
 └─────────────────────────────────────────────┘
        │  JDBC
        ▼
      MySQL
```

## 4. Folder Structure

```
student-management-system/
├── pom.xml                         Maven build file
├── Jenkinsfile                     Jenkins pipeline
├── Dockerfile                      Builds the app image from the Maven JAR
├── docker-compose.yml              Runs app + MySQL
├── .dockerignore
├── .env.example                    Sample DB credentials for Docker Compose
├── .gitignore
├── README.md
├── docs/
│   └── PROJECT_GUIDE.md            Maven/Jenkins/Docker concepts + viva guide
└── src/
    ├── main/
    │   ├── java/com/college/sms/
    │   │   ├── StudentManagementApplication.java
    │   │   ├── config/        SecurityConfig, RoleBasedSuccessHandler, DataInitializer
    │   │   ├── controller/    AuthController, TeacherDashboardController, TeacherStudentController,
    │   │   │                  TeacherAttendanceController, TeacherMarksController,
    │   │   │                  TeacherAnnouncementController, StudentPortalController, GlobalExceptionHandler
    │   │   ├── dto/           DashboardStats, AttendanceSummary, SubjectPerformance
    │   │   ├── entity/        User, Role, Student, Subject, Marks, Attendance, Announcement
    │   │   ├── repository/    UserRepository, StudentRepository, SubjectRepository,
    │   │   │                  MarksRepository, AttendanceRepository, AnnouncementRepository
    │   │   └── service/       CustomUserDetailsService, StudentService, SubjectService, MarksService,
    │   │                      AttendanceService, AnnouncementService, DashboardService
    │   └── resources/
    │       ├── application.properties
    │       ├── static/css/style.css
    │       ├── static/js/app.js
    │       └── templates/
    │           ├── fragments/layout.html     shared head, sidebars, top bar, alerts
    │           ├── login.html, access-denied.html, error.html
    │           ├── teacher/  dashboard, students, student-form, student-view, attendance, marks, announcements
    │           └── student/  dashboard, profile, marks, attendance, announcements
    └── test/
        ├── java/com/college/sms/
        │   ├── WebApplicationTest.java          login, role access, every page, add/delete student
        │   ├── entity/MarksTest.java            grade calculation
        │   └── service/StudentServiceTest.java, AttendanceServiceTest.java   (Mockito unit tests)
        └── resources/application.properties     H2 settings for tests
```

## 5. Database Design

Tables are created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

| Table | Columns | Notes |
|---|---|---|
| `users` | id (PK), username (unique), password (BCrypt), role (TEACHER/STUDENT) | login accounts |
| `students` | id (PK), student_code (unique), full_name, email (unique), department, study_year, phone, user_id (FK → users, unique) | one student ↔ one login |
| `subjects` | id (PK), code (unique), name | |
| `marks` | id (PK), student_id (FK), subject_id (FK), marks_obtained | unique (student_id, subject_id) |
| `attendance` | id (PK), student_id (FK), subject_id (FK), attendance_date, present (boolean) | unique (student_id, subject_id, attendance_date) |
| `announcements` | id (PK), title, message, created_at, posted_by | |

Relationships:

```
users 1 ──── 0..1 students
students 1 ──── * marks * ──── 1 subjects
students 1 ──── * attendance * ──── 1 subjects
announcements (independent)
```

Demo data inserted on first start: 1 teacher, 6 students, 5 subjects, marks for every student/subject, attendance for the last 10 weekdays, 3 announcements.

## 6. Application Workflow

1. User opens `http://localhost:8080` → not logged in → redirected to `/login`.
2. User submits username/password → Spring Security loads the user from MySQL (`CustomUserDetailsService`) and checks the BCrypt password.
3. Wrong password → `/login?error` shows a message. Correct → `RoleBasedSuccessHandler` redirects a TEACHER to `/teacher/dashboard` and a STUDENT to `/student/dashboard`.
4. Every request to `/teacher/**` requires ROLE_TEACHER and every request to `/student/**` requires ROLE_STUDENT; otherwise the "Access denied" page (HTTP 403) is shown.
5. Controller → Service → Repository → MySQL, then the controller puts data in the `Model` and Thymeleaf renders HTML.
6. Forms are validated (`@Valid`); errors are shown next to fields. After saving, a success message is shown (flash attribute).
7. Logout (`POST /logout`) ends the session and returns to the login page.

## 7. DevOps Workflow

```
Developer ──git push──▶ GitHub ──▶ Jenkins pipeline (Jenkinsfile)
                                     1. Checkout      get code from GitHub
                                     2. Build         mvn clean compile
                                     3. Test          mvn test   (H2 DB, JUnit report)
                                     4. Package       mvn package -DskipTests → target/student-management-system.jar
                                     5. Docker Build  docker build → student-management-system:<build no.> and :latest
                                                        │
                                                        ▼
                                     docker compose up -d  →  app container + MySQL container (+ volume)
```

If any stage fails, the later stages do not run – so a Docker image is only built from code whose tests passed.

---

## 8. Prerequisites

| Tool | Version | Check |
|---|---|---|
| JDK | 17 (or newer) | `java -version` |
| Maven | 3.8+ | `mvn -v` |
| MySQL | 8.x (only for running without Docker) | `mysql --version` |
| Docker Desktop | latest (includes Docker Compose) | `docker -v`, `docker compose version` |
| Git | any | `git --version` |
| Jenkins | LTS | open `http://localhost:8080` (see note on ports below) |

> **Port note:** Jenkins and this app both use port 8080 by default. If Jenkins runs on the same PC, either run Jenkins on another port (e.g. 9090) or start the app with `SERVER_PORT=8081`.

## 9. Run Locally (without Docker)

### 9.1 Database setup

Install MySQL and make sure it is running. The app creates the database `student_db` automatically (`createDatabaseIfNotExist=true`).

Default connection settings (from `application.properties`):

| Variable | Default |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `3306` |
| `DB_NAME` | `student_db` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |

If your MySQL password is different, set the environment variable before starting:

```bash
# Windows (Command Prompt)
set DB_PASSWORD=your_mysql_password

# Windows (PowerShell)
$env:DB_PASSWORD="your_mysql_password"

# Linux / macOS
export DB_PASSWORD=your_mysql_password
```

Optional – create a dedicated user instead of root:

```sql
CREATE DATABASE IF NOT EXISTS student_db;
CREATE USER 'sms_user'@'localhost' IDENTIFIED BY 'sms_pass';
GRANT ALL PRIVILEGES ON student_db.* TO 'sms_user'@'localhost';
FLUSH PRIVILEGES;
```
(then set `DB_USERNAME=sms_user` and `DB_PASSWORD=sms_pass`).

### 9.2 Build and run

```bash
mvn clean package                                # compile, test, create the JAR
java -jar target/student-management-system.jar   # start the app
# or, during development:
mvn spring-boot:run
```

Open **http://localhost:8080**.

## 10. Demo Login Credentials

| Role | Username | Password |
|---|---|---|
| Teacher | `teacher` | `teacher123` |
| Student | `student` | `student123` (linked to STU001 – Rahul Sharma) |

Other demo students: `stu002` … `stu006`, password `student123`.
Students added by a teacher get username = student ID in lower case (e.g. `stu007`) and password `student123`.

## 11. Maven Commands

| Command | What it does |
|---|---|
| `mvn clean` | Deletes the `target/` folder |
| `mvn compile` | Compiles `src/main/java` into `target/classes` |
| `mvn test` | Compiles and runs all tests in `src/test/java` (reports in `target/surefire-reports`) |
| `mvn package` | Runs tests and builds `target/student-management-system.jar` |
| `mvn clean package` | Fresh full build (the main command) |
| `mvn package -DskipTests` | Build the JAR without running tests |
| `mvn spring-boot:run` | Run the app directly from source |

## 12. Docker Commands

```bash
# 1. Build the JAR (the Docker image uses it)
mvn clean package

# 2. (optional) create your .env with DB credentials
copy .env.example .env        # Windows
cp .env.example .env          # Linux / macOS

# 3. Build the image and start app + MySQL with ONE command
docker compose up -d --build

# Build only the application image
docker build -t student-management-system:latest .

# Check running containers
docker ps
docker compose ps

# View logs
docker compose logs -f app
docker compose logs mysql

# Stop containers (keeps them)
docker compose stop

# Start them again
docker compose start

# Stop and remove containers + network (data is KEPT in the volume)
docker compose down

# Remove everything INCLUDING the database volume (deletes all data)
docker compose down -v

# List images / volumes
docker images
docker volume ls
```

Open **http://localhost:8081** once `docker compose logs -f app` shows `Started StudentManagementApplication`.
(Docker publishes the app on port **8081** so it does not clash with Jenkins on 8080; change `APP_PORT` in `.env` if needed.)
MySQL from Docker is reachable on your PC at `localhost:3307` (user `sms_user`, password `sms_pass`).

**Persistence check:** add a student → `docker compose down` → `docker compose up -d` → the student is still there (stored in the `mysql_data` volume).

## 13. Jenkins Setup

1. **Install Jenkins** (LTS) and finish the setup wizard with **"Install suggested plugins"** (includes Git, Pipeline and JUnit plugins). Change Jenkins' port if the app also needs 8080.
2. **Configure tools:** *Manage Jenkins → Tools*
   - *JDK installations → Add JDK*: Name **`JDK17`**, JAVA_HOME = your JDK 17 folder (or tick "Install automatically").
   - *Maven installations → Add Maven*: Name **`Maven3`**, tick "Install automatically" (choose 3.9.x).
   The names must match the `tools { }` block in the `Jenkinsfile`.
3. **Docker access:** Jenkins must be able to run `docker`. On Windows, install Docker Desktop and keep it running (Jenkins installed as a Windows service may need to run as your user account: *Services → Jenkins → Log On*). On Linux run `sudo usermod -aG docker jenkins` and restart Jenkins.
4. **Create the job:** *New Item* → name `student-management-system` → **Pipeline** → OK.
5. In **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/<your-username>/student-management-system.git`
   - Credentials: none for a public repo (for a private repo add a GitHub username + Personal Access Token)
   - Branch Specifier: `*/main`
   - Script Path: `Jenkinsfile`
6. *Save* → **Build Now**. Watch the stages in *Stage View* / *Console Output*. Test results appear under *Test Result*, the JAR under *Build Artifacts*.
7. (Optional) Automatic builds: *Build Triggers → Poll SCM* with `H/5 * * * *` (checks GitHub every 5 minutes).
8. After a green build, start the app from the built image: `docker compose up -d` and open http://localhost:8081.

## 14. Upload to GitHub

1. Create an empty repository on GitHub named `student-management-system` (no README).
2. In the project folder:

```bash
git init
git add .
git commit -m "Student Management System - DevOps mini project"
git branch -M main
git remote add origin https://github.com/<your-username>/student-management-system.git
git push -u origin main
```

`.gitignore` keeps `target/` and `.env` out of the repository.

## 15. Main URLs

| URL | Who | Purpose |
|---|---|---|
| `/login` | everyone | login page |
| `/teacher/dashboard` | teacher | statistics and charts |
| `/teacher/students` (`?keyword=`) | teacher | list / search students |
| `/teacher/students/new`, `/{id}`, `/{id}/edit` | teacher | add, view, edit |
| `POST /teacher/students/{id}/delete` | teacher | delete |
| `/teacher/attendance?subjectId=&date=` | teacher | mark attendance, view percentages |
| `/teacher/marks?subjectId=` | teacher | add / update marks |
| `/teacher/announcements` | teacher | create / delete announcements |
| `/student/dashboard`, `/profile`, `/marks`, `/attendance`, `/announcements` | student | own data only |

## 16. Troubleshooting

| Problem | Fix |
|---|---|
| `Access denied for user 'root'@'localhost'` | Set `DB_PASSWORD` (and `DB_USERNAME`) to your MySQL credentials |
| `Communications link failure` | MySQL is not running, or wrong `DB_HOST`/`DB_PORT` |
| `Port 8080 already in use` / `ports are not available` | Another program (often Jenkins) uses the port. Docker uses 8081 by default (`APP_PORT` in `.env`); for `java -jar` run with `SERVER_PORT=8081` |
| Docker build: `target/student-management-system.jar not found` | Run `mvn clean package` before `docker build` / `docker compose up --build` |
| App container restarts at first start | MySQL is still initialising; wait ~30 s and check `docker compose logs -f app` |
| Jenkins: `Tool type "maven" does not have an install of "Maven3"` | Add the tools in *Manage Jenkins → Tools* with exactly the names `JDK17` and `Maven3` |
| Jenkins: `docker: command not found` / permission denied | Install Docker on the Jenkins machine and give the Jenkins user access |
| Icons or charts missing | They load from a CDN – the browser needs internet access; the app itself still works |

See **docs/PROJECT_GUIDE.md** for the Maven, Jenkins and Docker concepts and a viva explanation.
