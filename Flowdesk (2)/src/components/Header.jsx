import { Bell, User, LogOut, Settings, LayoutDashboard } from 'lucide-react';
import logo from "../assets/logo.png";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../assets/axios";
import { logout, getCurrentUser } from "../utils/auth";

export default function Header() {
    const navigate = useNavigate();
    const user = getCurrentUser();

    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [notifOpen, setNotifOpen] = useState(false);

    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);

    // 🔔 Notification sound
    const sound = new Audio("/src/assets/notification.mp3");
    sound.volume = 0.4;

    // 📌 Load notifications + unread count
    const loadNotifications = async () => {
        try {
            const [notifRes, unreadRes] = await Promise.all([
                api.get("/notifications"),
                api.get("/notifications/unread-count")
            ]);

            setNotifications(notifRes.data || []);
            setUnreadCount(unreadRes.data.count || 0);
        } catch (err) {
            console.error("Failed to load notifications", err);
        }
    };

    // 🔄 Poll every 5 seconds + play sound
    useEffect(() => {
        loadNotifications();

        const interval = setInterval(async () => {
            try {
                const res = await api.get("/notifications/unread-count");
                const newCount = res.data.count || 0;

                // 🔔 Play sound only when unread increases
                if (newCount > unreadCount) {
                    sound.play();
                }

                setUnreadCount(newCount);
            } catch { }
        }, 5000);

        return () => clearInterval(interval);
    }, [unreadCount]);

    // 📌 Click → mark read → open task details
    const handleNotificationClick = async (notif) => {
        try {
            await api.post(`/notifications/${notif.id}/mark-read`);
        } catch { }

        if (notif.taskId) {
            navigate(`/tasks/${notif.taskId}`);
        }
        setNotifOpen(false);
        loadNotifications();
    };

    // 📌 Mark all notifications as read
    const handleMarkAllRead = async () => {
        try {
            await api.post("/notifications/mark-all-read");
            setUnreadCount(0);
            loadNotifications();
        } catch (err) {
            console.error("Failed to mark all read", err);
        }
    };

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    return (
        <header className="bg-white h-16 flex items-center justify-between px-8 shadow">

            {/* Logo */}
            <div className="flex items-center gap-3 cursor-pointer" onClick={() => navigate("/")}>
                <img src={logo} className="h-10" alt="FlowDesk" />
                <h1 className="text-2xl font-bold text-blue-600">FlowDesk</h1>
            </div>

            <div className="flex items-center gap-6">

                {/* Dashboard */}
                <button
                    onClick={() => navigate("/")}
                    className="flex items-center gap-2 text-gray-700 hover:text-black"
                >
                    <LayoutDashboard size={22} />
                    <span className="font-medium hidden md:block">Dashboard</span>
                </button>

                {/* 🔔 Notifications */}
                <div className="relative">
                    <div
                        className="cursor-pointer relative"
                        onClick={() => setNotifOpen(!notifOpen)}
                    >
                        <Bell size={24} className="text-gray-600 hover:text-black" />

                        {/* 🔴 Red unread badge */}
                        {unreadCount > 0 && (
                            <span className="absolute -top-1 -right-1 bg-red-600 text-white text-xs px-1.5 py-0.5 rounded-full">
                                {unreadCount}
                            </span>
                        )}
                    </div>

                    {/* 🔽 Notification Dropdown */}
                    {notifOpen && (
                        <div className="absolute right-0 mt-3 w-80 bg-white rounded-lg shadow-xl z-50 max-h-96 overflow-y-auto">

                            <div className="flex justify-between items-center p-3 border-b">
                                <h3 className="font-semibold">Notifications</h3>
                                <button
                                    onClick={handleMarkAllRead}
                                    className="text-sm text-blue-600 hover:underline"
                                >
                                    Mark all as read
                                </button>
                            </div>

                            {notifications.length === 0 ? (
                                <p className="p-4 text-center text-gray-500">No notifications</p>
                            ) : (
                                notifications.map((n) => (
                                    <div
                                        key={n.id}
                                        onClick={() => handleNotificationClick(n)}
                                        className={`p-4 border-b cursor-pointer relative ${n.read ? "bg-white" : "bg-blue-50 font-semibold"
                                            } hover:bg-gray-100`}
                                    >
                                        {/* 🔵 Unread dot */}
                                        {!n.read && (
                                            <span className="absolute left-2 top-1/2 -translate-y-1/2 h-2 w-2 bg-blue-600 rounded-full"></span>
                                        )}

                                        <p className="text-sm pl-4">{n.message}</p>
                                        <p className="text-xs text-gray-500 pl-4 mt-1">
                                            {new Date(n.createdAt).toLocaleString()}
                                        </p>
                                    </div>
                                ))
                            )}
                        </div>
                    )}
                </div>

                {/* Profile Dropdown */}
                <div className="relative">
                    <div
                        className="flex items-center gap-2 cursor-pointer hover:bg-gray-100 p-2 rounded-lg"
                        onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    >
                        <User size={24} />
                        <span className="font-medium">{user?.firstName || "User"}</span>
                    </div>

                    {isDropdownOpen && (
                        <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg z-50">
                            <div className="p-4 border-b">
                                <p className="font-semibold">{user?.firstName} {user?.lastName}</p>
                                <p className="text-sm text-gray-600">{user?.email}</p>
                                <p className="text-xs text-gray-500 uppercase mt-1">{user?.role}</p>
                            </div>

                            <a href="/profile" className="flex items-center gap-2 px-4 py-2 hover:bg-gray-100">
                                <Settings size={18} />
                                <span>Profile</span>
                            </a>

                            <button
                                onClick={handleLogout}
                                className="w-full flex items-center gap-2 px-4 py-2 text-red-600 hover:bg-red-50"
                            >
                                <LogOut size={18} />
                                <span>Logout</span>
                            </button>
                        </div>
                    )}
                </div>

            </div>
        </header>
    );
}
