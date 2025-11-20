export default function HighRiskTaskCard({ task }) {
    return (
        <div className="bg-red-50 border border-red-300 p-4 rounded-xl">
            <h3 className="font-semibold text-red-700">{task.title}</h3>
            <p className="text-sm">Assigned to: <span className="font-medium">{task.assignee}</span></p>
            <p className="text-sm">Deadline: <span className="font-semibold">{task.deadline}</span></p>
        </div>
    );
}
