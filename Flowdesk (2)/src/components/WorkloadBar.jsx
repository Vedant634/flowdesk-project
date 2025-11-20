export default function WorkloadBar({ name, load, capacity }) {
    const percent = (load / capacity) * 100;

    return (
        <div className="mb-4">
            <div className="flex justify-between text-sm font-medium">
                <span>{name}</span>
                <span>{load}/{capacity} hrs</span>
            </div>

            <div className="w-full bg-gray-200 rounded-full h-3 mt-1">
                <div
                    className={`h-3 rounded-full ${percent > 80 ? "bg-red-500" : percent > 50 ? "bg-yellow-500" : "bg-green-500"
                        }`}
                    style={{ width: `${percent}%` }}
                />
            </div>
        </div>
    );
}
