import logo from "../assets/logo.png";
import { Outlet, useLocation } from "react-router-dom";

export default function Layout() {
    const location = useLocation();
    const isAuthPage = location.pathname === "/login" || location.pathname === "/register";

    return (
        <div className="min-h-screen flex flex-col from-blue-200 via-blue-300 to-blue-400 bg-gradient-to-br">
            {/* Top Logo Bar */}
            <header className="w-full bg-gray-300 shadow-md py-3 px-6 flex items-center">
                <img src={logo || "/placeholder.svg"} alt="FlowDesk Logo" className="h-10" />
                <span className="ml-3 text-2xl font-bold text-blue-600">FlowDesk</span>
            </header>

            {/* Page Content */}
            <main className={isAuthPage ? "flex-1 flex justify-center items-center" : "flex-1"}>
                <Outlet />
            </main>
        </div>
    );
}
