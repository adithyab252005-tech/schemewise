import { useState, useEffect } from 'react';
import { fetchLogs } from '../services/api';

const UpdateLogs = () => {
    const [logs, setLogs] = useState([]);

    useEffect(() => {
        fetchLogs().then(data => setLogs(data)).catch(() => setLogs([]));
    }, []);

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6">System Update Logs</h2>

            <div className="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>Scheme</th>
                            <th>Version</th>
                            <th>Change Summary</th>
                            <th>Performed By</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {logs.map((log, i) => (
                            <tr key={i}>
                                <td className="font-bold">{log.scheme_name}</td>
                                <td>v{log.version}</td>
                                <td>{log.summary}</td>
                                <td>{log.user || 'System Crawler'}</td>
                                <td className="text-muted">{new Date(log.timestamp).toLocaleString()}</td>
                            </tr>
                        ))}
                        {logs.length === 0 && (
                            <tr><td colSpan="5" className="text-center p-4">No audit logs established yet.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default UpdateLogs;
