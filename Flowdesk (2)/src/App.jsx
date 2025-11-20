import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import AuthLayout from "./components/AuthLayout";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";

import ManagerDashboard from "./pages/ManagerDashboard";
import DeveloperDashboard from "./pages/DeveloperDashboard";
import ProjectsList from "./pages/ProjectsList";
import ProjectDetail from "./pages/ProjectDetail";
import TeamsList from "./pages/TeamsList";
import TeamDetail from "./pages/TeamDetail";
import TaskDetail from "./pages/TaskDetail";
import ProfilePage from "./pages/ProfilePage";
import CreateProject from "./pages/manager/CreateProject";
import CreateTeam from "./pages/manager/CreateTeam";
import CreateTaskPage from "./pages/manager/CreateTask";
import AssignTaskAI from "./pages/manager/AssignTaskAI";
import TeamWorkloadPage from "./pages/manager/TeamWorkloadPage";


export default function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* Public Routes */}
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Route>

        {/* Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<RoleBasedDashboard />} />

          {/* FIXED: Correct create routes */}
          <Route path="/projects/create" element={<CreateProject />} />
          <Route path="/teams/create" element={<CreateTeam />} />

          <Route path="/projects" element={<ProjectsList />} />
          <Route path="/projects/:id" element={<ProjectDetail />} />

          <Route path="/teams" element={<TeamsList />} />
          <Route path="/teams/:id" element={<TeamDetail />} />

          <Route path="/tasks/:id" element={<TaskDetail />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/projects/:projectId/tasks/create" element={<CreateTaskPage />} />
          <Route path="/tasks/:taskId/assign" element={<AssignTaskAI />} />
          <Route path="/teams/:teamId/workload" element={<TeamWorkloadPage />} />


        </Route>

        {/* Catch-all */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
}

function RoleBasedDashboard() {
  const user = JSON.parse(localStorage.getItem("currentUser"));

  if (!user) return <Navigate to="/login" />;

  return user.role === "MANAGER" ? (
    <ManagerDashboard />
  ) : (
    <DeveloperDashboard />
  );
}
