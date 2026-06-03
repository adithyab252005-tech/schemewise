import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { updateScheme } from '../services/api';

const EditScheme = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [changelog, setChangelog] = useState('');

    // NOTE: Pre-filling logic requires fetching the scheme by ID.
    // We'll keep this simple for the boilerplate requirements.

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await updateScheme(id, { changelog: changelog }); // Payload depends on backend implementation
            navigate('/admin/registry');
        } catch (err) {
            alert("Failed to update scheme. Check backend endpoint.");
            console.error(err);
        }
    };

    return (
        <div style={{ maxWidth: '800px' }}>
            <h2 className="text-2xl font-bold mb-6">Edit Scheme #{id}</h2>

            <div className="card">
                <p className="text-muted mb-6">Edits will automatically increment the scheme version to preserve audit history.</p>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label">Change Log Context</label>
                        <textarea
                            required
                            className="form-control"
                            rows="4"
                            placeholder="Briefly describe what rules changed..."
                            value={changelog}
                            onChange={(e) => setChangelog(e.target.value)}
                        />
                    </div>

                    <div className="form-group mt-6">
                        <button type="submit" className="btn btn-primary">Publish New Version</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditScheme;
