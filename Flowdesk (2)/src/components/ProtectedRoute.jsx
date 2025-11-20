import { Navigate, Outlet } from "react-router-dom";
import { isAuthenticated, getCurrentUser } from "../utils/auth";

export default function ProtectedRoute({ requiredRole }) {
    if (!isAuthenticated()) {
        return <Navigate to="/login" replace />;
    }

    if (requiredRole) {
        const user = getCurrentUser();
        if (user?.role !== requiredRole) {
            return <Navigate to="/" replace />;
        }
    }

    return <Outlet />;
}
