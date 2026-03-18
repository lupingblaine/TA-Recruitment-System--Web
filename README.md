# TA&MO — TA Recruitment & Management System

> **This is a UI prototype codebase.** The interactive prototype was originally designed in [Figma](https://www.figma.com/design/i7zh49YYZS7ajHqBgEpBrQ/TA-MO) and exported as a front-end code bundle for demonstration and review purposes. The final production system will be rebuilt in **Java**.

---

## Project Overview

TA&MO is a university-oriented **Teaching Assistant recruitment and management platform** that supports three user roles:

| Role | Key Features |
|------|-------------|
| **Applicant** | Browse & apply for TA positions, track application progress, manage profile & CV |
| **Organiser (Professor)** | Manage TA job postings, review applications via recruitment pipeline, schedule interviews |
| **Admin** | Analytics dashboard, workload monitoring, module & user management, system logs |

## Prototype Tech Stack

This prototype is built with the following technologies for rapid UI demonstration:

- **Framework:** React 18 + TypeScript
- **Build Tool:** Vite
- **Routing:** React Router 7
- **UI Components:** shadcn/ui (Radix UI) + MUI
- **Styling:** Tailwind CSS 4
- **Charts:** Recharts
- **Animation:** Motion
- **Forms:** React Hook Form

## Project Structure

```
src/
├── main.tsx                  # Entry point
├── styles/                   # Global styles & Tailwind config
├── app/
│   ├── App.tsx               # Root component
│   ├── routes.tsx            # Route definitions
│   ├── components/
│   │   ├── Layout.tsx        # Shared layout
│   │   └── ui/               # 40+ reusable UI components (shadcn/ui)
│   └── pages/
│       ├── Landing.tsx       # Landing page
│       ├── auth/             # Login / Signup
│       ├── applicant/        # Applicant portal (Dashboard, Jobs, Profile, Tracker)
│       ├── organiser/        # Organiser portal (Dashboard, Jobs, Pipeline, Interviews)
│       └── admin/            # Admin portal (Dashboard, Workload, Modules, Users, Logs)
```

## Running the Prototype

```bash
npm install
npm run dev
```

## Roadmap

This prototype serves as the UI/UX reference for the production system. The next steps include:

1. **Rebuild with Java** — Migrate to a Java-based architecture (Spring Boot back-end + appropriate front-end framework) for the final deliverable.
2. **Implement business logic** — Add authentication, database integration, API layer, and core recruitment workflows.
3. **Testing & deployment** — Unit tests, integration tests, and CI/CD pipeline.

## Authors

**Group 100** — EBU6304 Software Engineering

| Member |
|--------|
| Yifan Tang |
| Junjie Chen |
| Bingjun Han |
| Wenjun Du |
| Yiben Liu |

## License

This project is developed for academic purposes as part of the EBU6304 coursework.
