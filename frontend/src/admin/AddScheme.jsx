import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { addScheme } from '../services/api';

const AddScheme = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        scheme_name: '',
        source_url: '',
        state_applicable: 'ALL',
        income_min: '',
        income_max: '',
        rural_urban: 'Both',
        status: 'active'
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await addScheme(formData);
            navigate('/admin/registry');
        } catch (err) {
            alert("Failed to add scheme. Ensure backend POST /admin/add-scheme is implemented.");
            console.error(err);
        }
    };

    return (
        <div style={{ maxWidth: '800px' }}>
            <h2 className="text-2xl font-bold mb-6">Add New Scheme</h2>

            <div className="card">
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label">Scheme Name</label>
                        <input type="text" name="scheme_name" required className="form-control" onChange={handleChange} />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Source URL</label>
                        <input type="url" name="source_url" required className="form-control" onChange={handleChange} />
                    </div>

                    <div className="grid grid-cols-2 mb-4">
                        <div className="form-group">
                            <label className="form-label">State Applicable</label>
                            <input type="text" name="state_applicable" defaultValue="ALL" className="form-control" onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Location (Rural/Urban)</label>
                            <select name="rural_urban" className="form-control" onChange={handleChange}>
                                <option value="Both">Both</option>
                                <option value="Rural">Rural</option>
                                <option value="Urban">Urban</option>
                            </select>
                        </div>
                    </div>

                    <div className="grid grid-cols-2 mb-4">
                        <div className="form-group">
                            <label className="form-label">Min Income (₹)</label>
                            <input type="number" name="income_min" className="form-control" onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Max Income (₹)</label>
                            <input type="number" name="income_max" className="form-control" onChange={handleChange} />
                        </div>
                    </div>

                    <div className="form-group mt-6">
                        <button type="submit" className="btn btn-primary">Save Scheme Core Record</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddScheme;
