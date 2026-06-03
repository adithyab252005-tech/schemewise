import { useState, useEffect } from 'react';
import { useSchemes } from '../hooks/useSchemes';
import { useAdmin } from '../hooks/useAdmin';

const AdminOverview = () => {
    const { fetchAllSchemes } = useSchemes();
    const { fetchLogs } = useAdmin();

    const [stats, setStats] = useState({ total: 0, active: 0, inactive: 0 });
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadData = async () => {
            const schemesData = await fetchAllSchemes();
            if (schemesData) {
                setStats({
                    total: schemesData.length,
                    active: schemesData.filter(s => s.status === 'active').length,
                    inactive: schemesData.filter(s => s.status !== 'active').length
                });
            }

            const logsData = await fetchLogs();
            if (logsData) setLogs(logsData);

            setLoading(false);
        };
        loadData();
    }, [fetchAllSchemes, fetchLogs]);

    if (loading) return (
        <div className="container text-center" style={{ marginTop: '4rem' }}>
            <div className="text-xl font-bold mb-2">Loading Admin Dashboard</div>
            <div className="text-muted">Fetching system metrics and audit logs...</div>
        </div>
    );

    return (
        <div className="container" style={{ maxWidth: '1200px' }}>
            <h2 className="text-2xl font-bold mb-8">System Overview</h2>

            <div className="grid grid-cols-3 gap-6 mb-10">
                <div className="card text-center" style={{ padding: '2rem' }}>
                    <h3 className="text-muted text-sm font-semibold uppercase tracking-wide mb-3">Total Schemes in Registry</h3>
                    <div className="text-4xl font-bold text-main">{stats.total}</div>
                </div>
                <div className="card text-center" style={{ padding: '2rem' }}>
                    <h3 className="text-muted text-sm font-semibold uppercase tracking-wide mb-3">Active Protocols</h3>
                    <div className="text-4xl font-bold" style={{ color: 'var(--success)' }}>{stats.active}</div>
                </div>
                <div className="card text-center" style={{ padding: '2rem' }}>
                    <h3 className="text-muted text-sm font-semibold uppercase tracking-wide mb-3">Inactive / Deprecated</h3>
                    <div className="text-4xl font-bold" style={{ color: 'var(--danger)' }}>{stats.inactive}</div>
                </div>
            </div>

            <h3 className="text-xl font-bold mb-6">Recent Administrative Activity</h3>
            <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
                {logs.length > 0 ? (
                    <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ backgroundColor: 'var(--bg-color)', borderBottom: '1px solid var(--border-color)' }}>
                                <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Action Executed</th>
                                <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Target Entity ID</th>
                                <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Timestamp</th>
                            </tr>
                        </thead>
                        <tbody>
                            {logs.map((log, i) => (
                                <tr key={i} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                    <td style={{ padding: '1.25rem 1.5rem' }} className="font-bold text-main">{log.action}</td>
                                    <td style={{ padding: '1.25rem 1.5rem' }}>{log.scheme_id}</td>
                                    <td style={{ padding: '1.25rem 1.5rem' }} className="text-muted">{new Date(log.timestamp).toLocaleString()}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                ) : (
                    <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        No recent administrative activity found in the system audit logs.
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminOverview;
