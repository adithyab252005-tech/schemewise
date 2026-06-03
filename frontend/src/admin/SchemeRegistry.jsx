import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useSchemes } from '../hooks/useSchemes';
import { useAdmin } from '../hooks/useAdmin';
import StatusBadge from '../components/StatusBadge';

const SchemeRegistry = () => {
    const { fetchAllSchemes } = useSchemes();
    const { deleteScheme } = useAdmin();

    const [schemes, setSchemes] = useState([]);
    const [search, setSearch] = useState('');
    const [loading, setLoading] = useState(true);

    const loadSchemes = async () => {
        setLoading(true);
        const data = await fetchAllSchemes();
        if (data) setSchemes(data);
        setLoading(false);
    };

    useEffect(() => {
        loadSchemes();
    }, [fetchAllSchemes]);

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to logically deactivate this scheme? (This preserves historical evaluations)')) {
            try {
                await deleteScheme(id);
                loadSchemes();
            } catch (err) {
                alert('Failed to delete scheme. Check console.');
                console.error(err);
            }
        }
    };

    const filtered = schemes.filter(s => s.scheme_name?.toLowerCase().includes(search.toLowerCase()));

    if (loading) return (
        <div className="container text-center" style={{ marginTop: '4rem' }}>
            <div className="text-xl font-bold mb-2">Loading Scheme Registry</div>
            <div className="text-muted">Fetching all deterministically matched schemes...</div>
        </div>
    );

    return (
        <div className="container" style={{ maxWidth: '1200px' }}>
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h2 className="text-2xl font-bold mb-1">Scheme Registry</h2>
                    <div className="text-muted">Manage all extracted compliance rules and data</div>
                </div>
                <Link to="/admin/add" className="btn btn-primary">+ Add New Scheme</Link>
            </div>

            <div className="card mb-6" style={{ padding: '1rem' }}>
                <input
                    type="text"
                    placeholder="Search schemes by exact name..."
                    className="form-control"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    style={{ border: 'none', boxShadow: 'none', fontSize: '1rem' }}
                />
            </div>

            <div className="table-wrapper" style={{ borderRadius: 'var(--border-radius)', overflow: 'hidden', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-2xl shadow-black/5 dark:shadow-black/40)' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                        <tr style={{ backgroundColor: 'var(--bg-color)', borderBottom: '1px solid var(--border-color)' }}>
                            <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>ID</th>
                            <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Name</th>
                            <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Status</th>
                            <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Version</th>
                            <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Updated</th>
                            <th style={{ padding: '1rem 1.5rem', fontWeight: 600, color: 'var(--text-muted)' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map(s => (
                            <tr key={s.scheme_id} style={{ borderBottom: '1px solid var(--border-color)', backgroundColor: 'var(--card-bg)' }}>
                                <td style={{ padding: '1.25rem 1.5rem' }} className="text-muted text-sm">#{s.scheme_id}</td>
                                <td style={{ padding: '1.25rem 1.5rem' }} className="font-bold text-main">{s.scheme_name}</td>
                                <td style={{ padding: '1.25rem 1.5rem' }}>
                                    <StatusBadge status={s.status} />
                                </td>
                                <td style={{ padding: '1.25rem 1.5rem' }}>v{s.version || 1}</td>
                                <td style={{ padding: '1.25rem 1.5rem' }} className="text-muted text-sm">{new Date(s.last_updated).toLocaleDateString()}</td>
                                <td style={{ padding: '1.25rem 1.5rem' }}>
                                    <div className="flex gap-2">
                                        <Link to={`/admin/edit/${s.scheme_id}`} className="btn btn-outline text-sm" style={{ padding: '0.3rem 0.75rem' }}>Edit</Link>
                                        <button onClick={() => handleDelete(s.scheme_id)} className="btn btn-outline text-sm" style={{ padding: '0.3rem 0.75rem', color: 'var(--danger)', borderColor: 'var(--danger-bg)' }}>Deactivate</button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        {filtered.length === 0 && (
                            <tr><td colSpan="6" className="text-center text-muted" style={{ padding: '3rem' }}>No schemes found matching search criteria.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default SchemeRegistry;
