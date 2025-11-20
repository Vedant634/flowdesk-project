export default function StatsCard({ title, value, color }) {
    return (
        <div className="bg-white shadow rounded-xl p-6 border-l-4" style={{ borderColor: color }}>
            <p className="text-gray-600 font-medium">{title}</p>
            <h2 className="text-3xl font-bold mt-2">{value}</h2>
        </div>
    );
}
