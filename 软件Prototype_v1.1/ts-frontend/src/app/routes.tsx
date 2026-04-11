import { createBrowserRouter, RouterProvider } from "react-router";
import { Layout } from "./components/Layout";
import { Landing } from "./pages/Landing";
import { Login } from "./pages/auth/Login";

// Applicant
import { ApplicantDashboard } from "./pages/applicant/Dashboard";
import { ApplicantJobs } from "./pages/applicant/Jobs";
import { JobDetails } from "./pages/applicant/JobDetails";
import { ApplicationTracker } from "./pages/applicant/Tracker";
import { ApplicantProfile } from "./pages/applicant/Profile";

// Organiser
import { OrganiserDashboard } from "./pages/organiser/Dashboard";
import { OrganiserJobs } from "./pages/organiser/Jobs";
import { OrganiserInterviews } from "./pages/organiser/Interviews";
import { Pipeline } from "./pages/organiser/Pipeline";
import { OrganiserApplicationDetails } from "./pages/organiser/ApplicationDetails";

// Admin
import { AdminDashboard } from "./pages/admin/Dashboard";
import { AdminModules } from "./pages/admin/Modules";
import { AdminProperties } from "./pages/admin/Properties";
import { AdminUsers } from "./pages/admin/Users";
import { AdminLogs } from "./pages/admin/Logs";
import { AdminWorkload } from "./pages/admin/Workload";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Landing,
  },
  {
    path: "/auth/login",
    Component: Login,
  },
  {
    path: "/auth/signup",
    Component: Login, // Reuse login for prototype
  },
  {
    path: "/applicant",
    Component: Layout,
    children: [
      { index: true, Component: ApplicantDashboard },
      { path: "jobs", Component: ApplicantJobs },
      { path: "jobs/:id", Component: JobDetails },
      { path: "applications", Component: ApplicationTracker },
      { path: "profile", Component: ApplicantProfile },
    ],
  },
  {
    path: "/organiser",
    Component: Layout,
    children: [
      { index: true, Component: OrganiserDashboard },
      { path: "pipeline", Component: Pipeline },
      { path: "applications/:id", Component: OrganiserApplicationDetails },
      { path: "jobs", Component: OrganiserJobs },
      { path: "jobs/create", Component: OrganiserJobs },
      { path: "interviews", Component: OrganiserInterviews },
    ],
  },
  {
    path: "/admin",
    Component: Layout,
    children: [
      { index: true, Component: AdminDashboard },
      { path: "workload", Component: AdminWorkload },
      { path: "modules", Component: AdminModules },
      { path: "properties", Component: AdminProperties },
      { path: "users", Component: AdminUsers },
      { path: "logs", Component: AdminLogs },
    ],
  },
]);
