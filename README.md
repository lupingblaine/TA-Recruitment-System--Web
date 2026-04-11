# TA&MO — TA Recruitment & Management System

> University-oriented Teaching Assistant recruitment and management platform built with **Java Servlet/JSP** + **Embedded Tomcat**.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Access URLs](#access-urls)
- [User Roles & Test Accounts](#user-roles--test-accounts)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)
- [Archive: TypeScript Prototype](#archive-typescript-prototype)
- [License](#license)

---

## Project Overview

TA&MO is a full-stack web application for managing the TA recruitment lifecycle at a university. It supports three user roles:

| Role | Key Features |
|------|-------------|
| **Applicant (TA)** | Browse & apply for TA positions, track application status, manage profile & CV documents |
| **Organiser (Professor)** | Manage TA job postings, review applications via Kanban pipeline, schedule & manage interviews |
| **Admin** | Analytics dashboard, user & module management, workload monitoring, system logs, term & department configuration |

---

## Architecture

```
┌─────────────────────────────────────────────────┐
│              Embedded Tomcat (port 8080)         │
│                                                  │
│  ┌──────────────┐     ┌───────────────────────┐  │
│  │  JSP Frontend │     │  Servlet Backend API  │  │
│  │  (18 pages)   │────▶│  (42 REST endpoints)  │  │
│  │  Tailwind CSS │     │  Session Auth + RBAC  │  │
│  │  Vanilla JS   │     │  JSON file storage    │  │
│  └──────────────┘     └───────────────────────┘  │
│                              │                    │
│                        ┌─────▼─────┐              │
│                        │ data.json │              │
│                        └───────────┘              │
└─────────────────────────────────────────────────┘
```

- **Frontend**: JSP pages served by Tomcat, styled with Tailwind CSS CDN, interactive via vanilla JavaScript + Fetch API
- **Backend**: Java Servlets handling RESTful JSON APIs, with `CorsFilter` and `AuthFilter` for cross-origin and role-based access control
- **Data**: Single `data.json` file as persistent storage, managed by thread-safe `JsonDataService` singleton
- **Auth**: HTTP Session-based authentication; `AuthFilter` protects `/api/applicant/*`, `/api/organiser/*`, `/api/admin/*` endpoints

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Build Tool | Apache Maven | 3.6+ |
| Web Server | Embedded Apache Tomcat | 10.1.19 |
| Servlet API | Jakarta Servlet | 6.0.0 |
| JSP API | Jakarta JSP | 3.1.0 |
| JSON Parsing | Gson | 2.10.1 |
| CSS Framework | Tailwind CSS (CDN) | 4.x |
| Icons | Lucide Icons (CDN) | latest |
| Charts | Chart.js (CDN) | 4.x |

---

## Prerequisites

Before running the project, ensure you have the following installed:

### 1. Java Development Kit (JDK) 17+

```bash
# Verify installation
java -version
# Expected output: java version "17.x.x" or higher

javac -version
# Expected output: javac 17.x.x or higher
```

**Install options:**
- **macOS**: `brew install openjdk@17`
- **Ubuntu/Debian**: `sudo apt install openjdk-17-jdk`
- **Windows**: Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)

After installation, ensure `JAVA_HOME` is set:
```bash
# macOS/Linux
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # macOS
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64  # Ubuntu

# Windows (System Environment Variables)
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

### 2. Apache Maven 3.6+

```bash
# Verify installation
mvn -version
# Expected output: Apache Maven 3.6.x or higher
```

**Install options:**
- **macOS**: `brew install maven`
- **Ubuntu/Debian**: `sudo apt install maven`
- **Windows**: Download from [Maven Official](https://maven.apache.org/download.cgi), extract, and add `bin/` to `PATH`
- **Manual install (any OS)**:
  ```bash
  wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
  tar -xzf apache-maven-3.9.6-bin.tar.gz -C /opt
  export PATH="/opt/apache-maven-3.9.6/bin:$PATH"
  ```

### 3. Network Access

The application loads the following resources from CDN at runtime (browser-side):
- Tailwind CSS: `https://cdn.tailwindcss.com`
- Lucide Icons: `https://unpkg.com/lucide@latest`
- Chart.js: `https://cdn.jsdelivr.net/npm/chart.js`

Ensure the client browser has internet access, or the pages will load without styling/icons.

---

## Quick Start

### Step 1: Clone the Repository

```bash
git clone https://github.com/Elrond-JJ/Elrond-JJ-EBU6304-TA-Recruitment-System-Group100.git
cd Elrond-JJ-EBU6304-TA-Recruitment-System-Group100
git checkout dev
```

### Step 2: Navigate to Backend Directory

```bash
cd backend
```

### Step 3: Compile and Run

```bash
mvn compile exec:java
```

This single command will:
1. Download all Maven dependencies (first run only, ~30s)
2. Compile all Java source files
3. Start the embedded Tomcat server on port **8080**

**Expected output:**
```
========================================
  TA&MO 后端已启动: http://localhost:8080/tamo
  落地页: http://localhost:8080/tamo/index.jsp
  登录页: http://localhost:8080/tamo/auth/login.jsp
========================================
```

### Step 4: Open in Browser

Visit: **http://localhost:8080/tamo**

### Step 5: Stop the Server

Press `Ctrl + C` in the terminal to gracefully shut down.

---

## Project Structure

```
TA&MO_v1.0/
├── README.md                          # This file
├── .gitignore
├── ATTRIBUTIONS.md
├── LICENSE
├── EBU6304_GroupProjectHandout.pdf
│
├── backend/                           # Main application
│   ├── pom.xml                        # Maven project config
│   ├── TEST_DOCUMENT.md              # API test documentation (42 endpoints, 207 test cases)
│   │
│   └── src/main/
│       ├── java/com/tamo/
│       │   ├── Main.java              # Entry point — embedded Tomcat bootstrap
│       │   ├── filter/
│       │   │   ├── AuthFilter.java    # Session-based auth guard for /api/* endpoints
│       │   │   └── CorsFilter.java    # CORS headers for cross-origin requests
│       │   ├── servlet/
│       │   │   ├── AuthServlet.java       # /api/auth/*     (login, signup, logout, me)
│       │   │   ├── ApplicantServlet.java  # /api/applicant/* (jobs, applications, profile)
│       │   │   ├── OrganiserServlet.java  # /api/organiser/* (jobs, pipeline, interviews)
│       │   │   └── AdminServlet.java      # /api/admin/*     (users, modules, logs, workload)
│       │   ├── service/
│       │   │   ├── JsonDataService.java   # Thread-safe JSON file read/write (singleton)
│       │   │   ├── AuthService.java
│       │   │   ├── ApplicantService.java
│       │   │   ├── OrganiserService.java
│       │   │   └── AdminService.java
│       │   ├── model/                 # Data models (POJO)
│       │   │   ├── DataStore.java     # Root data container
│       │   │   ├── User.java
│       │   │   ├── Profile.java
│       │   │   ├── Job.java
│       │   │   ├── Application.java
│       │   │   ├── Interview.java
│       │   │   ├── Module.java
│       │   │   ├── Term.java
│       │   │   ├── Department.java
│       │   │   ├── LogEntry.java
│       │   │   └── WorkloadAssignment.java
│       │   └── util/
│       │       └── JsonUtil.java      # Gson instance with date formatting
│       │
│       ├── resources/
│       │   └── data.json              # Persistent data store (JSON file database)
│       │
│       └── webapp/
│           ├── WEB-INF/web.xml        # Servlet & filter declarations
│           ├── index.jsp              # Landing page (public)
│           ├── auth/
│           │   ├── login.jsp          # Login page
│           │   └── signup.jsp         # Registration page
│           ├── applicant/             # Applicant role pages (5)
│           │   ├── dashboard.jsp
│           │   ├── jobs.jsp
│           │   ├── job-detail.jsp
│           │   ├── applications.jsp
│           │   └── profile.jsp
│           ├── organiser/             # Organiser role pages (5)
│           │   ├── dashboard.jsp
│           │   ├── jobs.jsp
│           │   ├── pipeline.jsp
│           │   ├── application-detail.jsp
│           │   └── interviews.jsp
│           ├── admin/                 # Admin role pages (6)
│           │   ├── dashboard.jsp
│           │   ├── users.jsp
│           │   ├── modules.jsp
│           │   ├── properties.jsp
│           │   ├── logs.jsp
│           │   └── workload.jsp
│           ├── common/                # Shared JSP fragments
│           │   ├── head.jsp           # HTML head (meta, CDN links, Tailwind config)
│           │   ├── sidebar.jsp        # Navigation sidebar (role-aware)
│           │   ├── layout-top.jsp     # Page layout wrapper (top)
│           │   ├── layout-bottom.jsp  # Page layout wrapper (bottom)
│           │   └── auth-check.jsp     # Session auth guard (static include)
│           └── static/
│               ├── css/custom.css     # Custom component styles
│               └── js/api.js          # JavaScript API client (all 42 endpoints)
│
└── 软件Prototype_v1.1/                # Archived early-stage code
    ├── Adviser/                       # Admin UI prototype screenshots
    ├── MO/                            # Organiser UI prototype screenshots
    ├── TA/                            # Applicant UI prototype screenshots
    ├── Web_pages/                     # Landing & login screenshots
    └── ts-frontend/                   # Original TypeScript/React frontend source
        ├── src/                       # React components, pages, routes
        ├── package.json
        ├── vite.config.ts
        └── ...
```

---

## Access URLs

After starting the server, the following URLs are available:

### Public Pages (no login required)

| Page | URL |
|------|-----|
| Landing Page | http://localhost:8080/tamo/index.jsp |
| Login | http://localhost:8080/tamo/auth/login.jsp |
| Sign Up | http://localhost:8080/tamo/auth/signup.jsp |

### Applicant Pages (login required, role: applicant)

| Page | URL |
|------|-----|
| Dashboard | http://localhost:8080/tamo/applicant/dashboard.jsp |
| Browse Jobs | http://localhost:8080/tamo/applicant/jobs.jsp |
| Job Details | http://localhost:8080/tamo/applicant/job-detail.jsp?id={jobId} |
| My Applications | http://localhost:8080/tamo/applicant/applications.jsp |
| Profile | http://localhost:8080/tamo/applicant/profile.jsp |

### Organiser Pages (login required, role: organiser)

| Page | URL |
|------|-----|
| Dashboard | http://localhost:8080/tamo/organiser/dashboard.jsp |
| Manage Jobs | http://localhost:8080/tamo/organiser/jobs.jsp |
| Recruitment Pipeline | http://localhost:8080/tamo/organiser/pipeline.jsp |
| Application Detail | http://localhost:8080/tamo/organiser/application-detail.jsp?id={appId} |
| Interviews | http://localhost:8080/tamo/organiser/interviews.jsp |

### Admin Pages (login required, role: admin)

| Page | URL |
|------|-----|
| Dashboard | http://localhost:8080/tamo/admin/dashboard.jsp |
| User Management | http://localhost:8080/tamo/admin/users.jsp |
| Module Management | http://localhost:8080/tamo/admin/modules.jsp |
| Properties (Terms & Depts) | http://localhost:8080/tamo/admin/properties.jsp |
| System Logs | http://localhost:8080/tamo/admin/logs.jsp |
| Workload Management | http://localhost:8080/tamo/admin/workload.jsp |

---

## User Roles & Test Accounts

The application ships with pre-configured test accounts in `data.json`:

| Role | Name | Email | Password |
|------|------|-------|----------|
| Applicant | Alex Johnson | `alex.j@university.edu` | `hashed_password_1` |
| Applicant | Bob Smith | `bob.s@university.edu` | `hashed_password_2` |
| Applicant | Carol Williams | `carol.w@university.edu` | `hashed_password_3` |
| Organiser | Alan Turing | `alan@university.edu` | `hashed_password_5` |
| Organiser | Grace Hopper | `grace@university.edu` | `hashed_password_6` |
| Admin | System Admin | `admin@university.edu` | `admin_hashed` |

> **Note**: Passwords are stored as plaintext placeholders (prefixed `hashed_`). In the login flow, the password field is compared directly against this stored value. You can also register new accounts via the Sign Up page.

---

## API Reference

The backend exposes **42 RESTful API endpoints** under the `/tamo/api/` prefix. All protected endpoints require an active HTTP session (obtained via login).

### Authentication (`/api/auth/*`) — 4 endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login with email & password |
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/logout` | Destroy session |
| GET | `/api/auth/me` | Get current logged-in user info |

### Applicant (`/api/applicant/*`) — 10 endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/applicant/dashboard` | Dashboard statistics |
| GET | `/api/applicant/jobs` | List available jobs (supports query filters) |
| GET | `/api/applicant/jobs/{id}` | Job details |
| POST | `/api/applicant/jobs/{jobId}/apply` | Submit application |
| GET | `/api/applicant/applications` | List my applications |
| GET | `/api/applicant/applications/{id}` | Application details |
| GET | `/api/applicant/profile` | Get my profile |
| PUT | `/api/applicant/profile` | Update profile |
| POST | `/api/applicant/profile/documents` | Upload document |
| DELETE | `/api/applicant/profile/documents/{docId}` | Delete document |

### Organiser (`/api/organiser/*`) — 12 endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/organiser/dashboard` | Dashboard statistics |
| GET | `/api/organiser/jobs` | List managed jobs |
| POST | `/api/organiser/jobs` | Create new job posting |
| PUT | `/api/organiser/jobs/{id}` | Update job posting |
| DELETE | `/api/organiser/jobs/{id}` | Delete job posting |
| GET | `/api/organiser/pipeline` | Recruitment pipeline (Kanban view, `?module=` filter) |
| PUT | `/api/organiser/pipeline/{appId}/status` | Move application in pipeline |
| GET | `/api/organiser/applications/{id}` | Application details |
| PUT | `/api/organiser/applications/{id}/action` | Accept/reject application |
| GET | `/api/organiser/interviews` | List interviews |
| POST | `/api/organiser/interviews` | Schedule interview |
| PUT | `/api/organiser/interviews/{id}` | Update interview |

### Admin (`/api/admin/*`) — 16 endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/dashboard` | System-wide analytics |
| GET | `/api/admin/users` | List all users (supports query filters) |
| POST | `/api/admin/users` | Create user |
| PUT | `/api/admin/users/{id}` | Update user |
| DELETE | `/api/admin/users/{id}` | Delete user |
| GET | `/api/admin/modules` | List modules (`?keyword=` filter) |
| POST | `/api/admin/modules` | Create module |
| PUT | `/api/admin/modules/{id}` | Update module |
| DELETE | `/api/admin/modules/{id}` | Delete module |
| GET | `/api/admin/properties/terms` | List terms |
| POST | `/api/admin/properties/terms` | Create term |
| GET | `/api/admin/properties/departments` | List departments |
| POST | `/api/admin/properties/departments` | Create department |
| GET | `/api/admin/logs` | System logs (supports query filters) |
| GET | `/api/admin/workload` | Workload assignments (supports query filters) |
| PUT | `/api/admin/workload/{taId}/reassign` | Reassign TA workload |

> For detailed request/response schemas and test cases, see [`backend/TEST_DOCUMENT.md`](backend/TEST_DOCUMENT.md).

---

## Configuration

### Port

The server runs on port **8080** by default. To change it, modify `Main.java`:

```java
tomcat.setPort(8080);  // Change to desired port
```

### Data File

All application data is stored in `backend/src/main/resources/data.json`. The service reads/writes this file directly at runtime.

To reset data to defaults, restore the original `data.json` from version control:

```bash
cd backend
git checkout -- src/main/resources/data.json
```

### Context Path

The application is deployed under the `/tamo` context path. This is configured in `Main.java`:

```java
Context ctx = tomcat.addWebapp("/tamo", webappDir);
```

---

## Troubleshooting

### Port 8080 already in use

```bash
# Find and kill the process
lsof -ti :8080 | xargs kill -9

# Then restart
cd backend && mvn compile exec:java
```

### Maven not found

Ensure Maven is installed and in your PATH:
```bash
mvn -version
```

If not installed, see [Prerequisites](#prerequisites).

### Java version mismatch

The project requires **Java 17+**. Verify with:
```bash
java -version
```

If you have multiple Java versions, set `JAVA_HOME` explicitly:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS
```

### Pages load without CSS styling

The frontend uses Tailwind CSS from CDN. Ensure the browser has internet access. If running in an isolated environment, the pages will be functional but unstyled.

### Data changes not persisting

The application writes to `src/main/resources/data.json` at runtime. If you run `mvn compile` again, Maven copies the source file to `target/classes/`, but the runtime reads from the source directory directly. Changes will persist across restarts as long as you don't manually revert the file.

---

## Archive: TypeScript Prototype

The `软件Prototype_v1.1/` directory contains the archived early-stage codebase:

- **`ts-frontend/`**: Original React/TypeScript UI prototype (Vite + React 18 + shadcn/ui + Tailwind CSS 4)
- **`Adviser/`, `MO/`, `TA/`, `Web_pages/`**: UI prototype screenshots for each role

This code served as the design reference for the current Java Servlet/JSP implementation and is preserved for historical purposes.

---

## License

This project is developed by **Group 100** for the **EBU6304 Software Engineering** course.

See [LICENSE](LICENSE) for details.
