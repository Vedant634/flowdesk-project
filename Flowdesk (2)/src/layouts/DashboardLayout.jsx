import Header from "../components/Header";

export default function DashboardLayout({ children }) {
    return (
        <div className="min-h-screen bg-gray-100 flex flex-col">
            <Header />
            <main className="p-8 flex-1">{children}</main>
        </div>
    );
}
