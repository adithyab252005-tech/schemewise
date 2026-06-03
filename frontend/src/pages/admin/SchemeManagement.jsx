import { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';

const SchemeManagement = () => {
    const [schemes, setSchemes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editingScheme, setEditingScheme] = useState(null);
    const [editForm, setEditForm] = useState({});
    const [viewingScheme, setViewingScheme] = useState(null);
    const [viewData, setViewData] = useState(null);
    const [isScraping, setIsScraping] = useState(false);

    // New Modals State
    const [showAddModal, setShowAddModal] = useState(false);
    const [addForm, setAddForm] = useState({
        scheme_name: '', source_url: '', scheme_category: 'General', state_applicable: 'All India',
        income_max: '', target_age_min: '', target_age_max: '', status: 'active'
    });
    const [showBulkModal, setShowBulkModal] = useState(false);
    const [bulkFile, setBulkFile] = useState(null);
    const [bulkStatus, setBulkStatus] = useState(null);

    const fetchSchemes = async () => {
        setLoading(true);
        try {
            const res = await axios.get('http://180.235.121.245:8060/api/v1/schemes?active_only=false');
            setSchemes(res.data);
        } catch (error) {
            console.error("Failed to fetch schemes", error);
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchSchemes();
    }, []);

    const handleEditClick = async (schemeId) => {
        try {
            const res = await axios.get(`http://180.235.121.245:8060/api/v1/schemes/${schemeId}`);
            setEditingScheme(schemeId);
            setEditForm(res.data);
        } catch (error) {
            console.error("Failed to fetch scheme details", error);
        }
    };

    const handleViewClick = async (schemeId) => {
        try {
            const res = await axios.get(`http://180.235.121.245:8060/api/v1/schemes/${schemeId}`);
            setViewingScheme(schemeId);
            setViewData(res.data);
        } catch (error) {
            console.error("Failed to fetch scheme details for viewing", error);
        }
    };

    const handleScrapeDescription = async () => {
        if (!viewingScheme) return;
        setIsScraping(true);
        try {
            const res = await axios.post(`http://180.235.121.245:8060/api/v1/admin/schemes/${viewingScheme}/scrape-description`);
            setViewData(prev => ({ ...prev, content_hash: res.data.content_hash }));
        } catch (error) {
            console.error("Failed to scrape scheme description", error);
            alert("Scraping failed: " + (error.response?.data?.error || error.message));
        } finally {
            setIsScraping(false);
        }
    };

    const handleSaveEdit = async () => {
        try {
            await axios.put(`http://180.235.121.245:8060/api/v1/admin/schemes/${editingScheme}`, {
                target_age_min: editForm.target_age_min,
                target_age_max: editForm.target_age_max,
                income_max: editForm.income_max,
                status: editForm.status
            });
            setEditingScheme(null);
            fetchSchemes(); // Refresh list
        } catch (error) {
            console.error("Failed to update scheme", error);
            alert("Error updating scheme");
        }
    };

    const handleAddSubmit = async () => {
        try {
            await axios.post('http://180.235.121.245:8060/api/v1/admin/schemes/single', addForm);
            setShowAddModal(false);
            setAddForm({
                scheme_name: '', source_url: '', scheme_category: 'General', state_applicable: 'All India',
                income_max: '', target_age_min: '', target_age_max: '', status: 'active'
            });
            fetchSchemes();
        } catch (error) {
            console.error("Failed to add scheme", error);
            alert("Error adding scheme: " + (error.response?.data?.error || error.message));
        }
    };

    const handleBulkUpload = async () => {
        if (!bulkFile) return;
        setBulkStatus("Reading file...");
        try {
            const fileReader = new FileReader();
            fileReader.onload = async (e) => {
                try {
                    const jsonPayload = JSON.parse(e.target.result);
                    setBulkStatus(`Uploading ${jsonPayload.length} schemes...`);
                    await axios.post('http://180.235.121.245:8060/api/v1/admin/schemes/bulk', jsonPayload);
                    setBulkStatus("Upload complete! Refreshing database.");
                    setTimeout(() => {
                        setShowBulkModal(false);
                        setBulkFile(null);
                        setBulkStatus(null);
                        fetchSchemes();
                    }, 1500);
                } catch (err) {
                    setBulkStatus("Error: Invalid JSON format.");
                }
            };
            fileReader.readAsText(bulkFile);
        } catch (error) {
            console.error("Upload failed", error);
            setBulkStatus("Error uploading file.");
        }
    };

    return (
        <div className="max-w-7xl mx-auto">
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-3xl font-display font-bold text-white tracking-tight mb-2">Scheme Database</h1>
                    <p className="text-slate-300">Manage all 4,200+ schemes. Modify strict parameters or toggle visibility.</p>
                </div>
                <div className="flex gap-4">
                    <button onClick={() => setShowAddModal(true)} className="px-5 py-2.5 bg-brand-500 text-white font-medium hover:bg-brand-400 rounded-lg transition-colors shadow-lg shadow-brand-500/20">
                        + Add Manually
                    </button>
                    <button onClick={() => setShowBulkModal(true)} className="px-5 py-2.5 bg-white dark:bg-zinc-900 border border-slate-300 dark:border-white/10 text-slate-900 dark:text-white font-medium hover:bg-slate-50 dark:hover:bg-zinc-800 rounded-lg transition-colors flex items-center gap-2">
                        📥 Bulk Upload (JSON)
                    </button>
                </div>
            </div>

            <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl overflow-hidden">
                <div className="overflow-x-auto h-[600px] custom-scrollbar-dark">
                    {loading ? (
                        <div className="p-8 text-center text-slate-500 dark:text-zinc-400">Loading Schemes...</div>
                    ) : (
                        <table className="w-full text-left text-sm text-slate-500 dark:text-zinc-400">
                            <thead className="text-xs text-slate-500 dark:text-zinc-400 uppercase bg-white dark:bg-white dark:bg-zinc-900/50 sticky top-0">
                                <tr>
                                    <th className="px-6 py-4 font-medium">ID</th>
                                    <th className="px-6 py-4 font-medium">Scheme Name</th>
                                    <th className="px-6 py-4 font-medium">Sector</th>
                                    <th className="px-6 py-4 font-medium">Status</th>
                                    <th className="px-6 py-4 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-zinc-900/60">
                                {schemes.map((s, index) => (
                                    <tr key={s.scheme_id} className="hover:bg-white dark:bg-white/60 dark:bg-zinc-900/30 transition-colors">
                                        <td className="px-6 py-4 font-mono text-xs text-slate-500 dark:text-zinc-400 font-medium">{index + 1}</td>
                                        <td className="px-6 py-4 text-slate-500 dark:text-zinc-400 font-medium max-w-md truncate" title={s.scheme_name}>{s.scheme_name}</td>
                                        <td className="px-6 py-4">
                                            <span className="px-3 py-1.5 rounded-full bg-white dark:bg-zinc-900/80 border border-slate-200 dark:border-white/5 text-xs font-semibold whitespace-nowrap inline-flex items-center shadow-2xl shadow-black/5 dark:shadow-black/40">
                                                {s.scheme_category}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className={`px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap inline-flex items-center shadow-2xl shadow-black/5 dark:shadow-black/40 ${s.status === 'active' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'}`}>
                                                {s.status.toUpperCase()}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-2">
                                                <button onClick={() => handleViewClick(s.scheme_id)} className="px-3 py-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 border border-emerald-500/20 text-xs font-semibold transition-all">View</button>
                                                <button onClick={() => handleEditClick(s.scheme_id)} className="px-3 py-1.5 rounded-lg bg-brand-500/10 text-brand-400 hover:bg-brand-500/20 border border-brand-500/20 text-xs font-semibold transition-all">Edit</button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>

            {/* Edit Modal */}
            {
                editingScheme && (
                    <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                        <div className="bg-[#0a0a0a] border border-slate-300 dark:border-zinc-900 rounded-2xl w-full max-w-2xl overflow-hidden shadow-2xl">
                            <div className="px-6 py-5 border-b border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50 flex justify-between items-center">
                                <h3 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight truncate max-w-md">{editForm.scheme_name}</h3>
                                <button onClick={() => setEditingScheme(null)} className="text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:text-white">&times;</button>
                            </div>
                            <div className="p-6 space-y-6">
                                <div className="grid grid-cols-2 gap-6">
                                    <div>
                                        <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Target Age Min</label>
                                        <input type="number" value={editForm.target_age_min || ''} onChange={e => setEditForm({ ...editForm, target_age_min: parseInt(e.target.value) })} className="w-full bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="e.g. 18" />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Target Age Max</label>
                                        <input type="number" value={editForm.target_age_max || ''} onChange={e => setEditForm({ ...editForm, target_age_max: parseInt(e.target.value) })} className="w-full bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="e.g. 60" />
                                    </div>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Income Max Limit (₹)</label>
                                        <input type="number" value={editForm.income_max || ''} onChange={e => setEditForm({ ...editForm, income_max: parseFloat(e.target.value) })} className="w-full bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="e.g. 250000" />
                                    </div>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Status</label>
                                        <select value={editForm.status} onChange={e => setEditForm({ ...editForm, status: e.target.value })} className="w-full bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500">
                                            <option value="active">Active</option>
                                            <option value="inactive">Inactive</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div className="px-6 py-5 border-t border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50 flex justify-end gap-3">
                                <button onClick={() => setEditingScheme(null)} className="px-5 py-2.5 text-slate-500 dark:text-zinc-400 font-medium hover:bg-white dark:bg-zinc-900 rounded-lg transition-colors">Cancel</button>
                                <button onClick={handleSaveEdit} className="px-5 py-2.5 bg-brand-500 text-slate-900 dark:text-white font-medium hover:bg-brand-400 rounded-lg transition-colors">Save Parameters</button>
                            </div>
                        </div>
                    </div>
                )
            }

            {/* View Details Modal */}
            {
                viewingScheme && viewData && (
                    <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                        <div className="bg-[#0a0a0a] border border-slate-300 dark:border-zinc-900 rounded-2xl w-full max-w-4xl max-h-[85vh] flex flex-col overflow-hidden shadow-2xl">
                            <div className="px-6 py-5 border-b border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50 flex justify-between items-center shrink-0">
                                <div>
                                    <h3 className="text-xl font-display font-bold text-slate-900 dark:text-white tracking-tight max-w-2xl truncate">{viewData.scheme_name}</h3>
                                    <div className="flex gap-2 mt-2">
                                        <span className="px-2 py-0.5 rounded-full bg-brand-500/10 text-brand-400 border border-brand-500/20 text-xs font-medium">{viewData.scheme_category}</span>
                                        <span className="px-2 py-0.5 rounded-full bg-white dark:bg-zinc-900 text-slate-500 dark:text-zinc-400 border border-slate-200 dark:border-white/5 text-xs font-medium">{viewData.state_applicable}</span>
                                    </div>
                                </div>
                                <div className="flex items-center gap-4">
                                    <button
                                        onClick={handleScrapeDescription}
                                        disabled={isScraping}
                                        className={`px-4 py-2 rounded-lg font-medium text-sm transition-all flex items-center gap-2 ${isScraping ? 'bg-orange-600/20 text-orange-400 cursor-not-allowed border border-orange-500/30' : 'bg-orange-600 text-slate-900 dark:text-white hover:bg-orange-400 shadow-[0_0_15px_rgba(99,102,241,0.2)]'}`}
                                    >
                                        {isScraping ? '⏳ Scraping Bot Active...' : '✨ AI Fetch Details'}
                                    </button>
                                    <button onClick={() => setViewingScheme(null)} className="text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:text-white p-2">&times;</button>
                                </div>
                            </div>

                            <div className="p-6 overflow-y-auto custom-scrollbar-dark flex-1 space-y-8">
                                {/* Metadata Grid */}
                                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                                    <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-4">
                                        <p className="text-xs text-slate-500 dark:text-zinc-400 font-medium uppercase mb-1">Target Age</p>
                                        <p className="text-slate-600 dark:text-zinc-300 font-semibold">{viewData.target_age_min || 0} - {viewData.target_age_max || 'No limit'} years</p>
                                    </div>
                                    <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-4">
                                        <p className="text-xs text-slate-500 dark:text-zinc-400 font-medium uppercase mb-1">Target Gender</p>
                                        <p className="text-slate-600 dark:text-zinc-300 font-semibold">{viewData.target_gender || 'All'}</p>
                                    </div>
                                    <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-4">
                                        <p className="text-xs text-slate-500 dark:text-zinc-400 font-medium uppercase mb-1">Max Income</p>
                                        <p className="text-slate-600 dark:text-zinc-300 font-semibold">{viewData.income_max ? `₹${viewData.income_max.toLocaleString()}` : 'No limit'}</p>
                                    </div>
                                    <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-4">
                                        <p className="text-xs text-slate-500 dark:text-zinc-400 font-medium uppercase mb-1">Status</p>
                                        <p className={`font-semibold ${viewData.status === 'active' ? 'text-emerald-400' : 'text-rose-400'}`}>
                                            {viewData.status.toUpperCase()}
                                        </p>
                                    </div>
                                </div>

                                {/* Eligibility Tags */}
                                <div className="flex flex-wrap gap-2">
                                    {viewData.is_student === 'Yes' && <span className="px-3 py-1 bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 rounded-lg text-sm">Student Scheme</span>}
                                    {viewData.is_bpl === 'Yes' && <span className="px-3 py-1 bg-amber-500/10 text-amber-400 border border-amber-500/20 rounded-lg text-sm">BPL Required</span>}
                                    {viewData.is_differently_abled === 'Yes' && <span className="px-3 py-1 bg-purple-500/10 text-purple-400 border border-purple-500/20 rounded-lg text-sm">Differently Abled</span>}
                                    {viewData.is_minority === 'Yes' && <span className="px-3 py-1 bg-pink-500/10 text-pink-400 border border-pink-500/20 rounded-lg text-sm">Minority Focus</span>}
                                </div>

                                {/* Description / Content Hash Block */}
                                <div>
                                    <h4 className="text-sm font-bold text-slate-900 dark:text-white tracking-tight mb-3 flex items-center gap-2">
                                        <span>📄</span> Full Markdown Description
                                    </h4>
                                    <div className="bg-slate-50 dark:bg-zinc-950/50 border border-slate-300 dark:border-zinc-900 rounded-xl p-5 text-slate-500 dark:text-zinc-400 text-sm leading-relaxed whitespace-pre-wrap max-w-none">
                                        {viewData.content_hash ? (
                                            viewData.content_hash
                                        ) : (
                                            <p className="italic text-slate-500 dark:text-zinc-400">No detailed description available for this scheme.</p>
                                        )}
                                    </div>
                                </div>

                                {/* External Link */}
                                {viewData.source_url && (
                                    <div>
                                        <h4 className="text-sm font-bold text-slate-900 dark:text-white tracking-tight mb-3">Official Portal Link</h4>
                                        <a href={viewData.source_url} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-2 text-brand-400 hover:text-brand-300 break-all bg-brand-500/10 p-4 rounded-xl border border-brand-500/20 w-full transition-colors">
                                            🔗 {viewData.source_url}
                                        </a>
                                    </div>
                                )}
                            </div>

                            <div className="px-6 py-4 border-t border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50 flex justify-end shrink-0">
                                <button onClick={() => setViewingScheme(null)} className="px-6 py-2.5 bg-white dark:bg-zinc-900 text-slate-900 dark:text-white font-medium hover:bg-slate-200 dark:hover:bg-zinc-800 rounded-lg transition-colors">Close View</button>
                            </div>
                        </div>
                    </div>
                )
            }

            {/* Add Scheme Modal */}
            {
                showAddModal && (
                    <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                        <div className="bg-[#0a0a0a] border border-slate-300 dark:border-zinc-900 rounded-2xl w-full max-w-2xl overflow-hidden shadow-2xl flex flex-col max-h-[90vh]">
                            <div className="px-6 py-5 border-b border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50 flex justify-between items-center shrink-0">
                                <h3 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight">Add New Scheme</h3>
                                <button onClick={() => setShowAddModal(false)} className="text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:text-white">&times;</button>
                            </div>
                            <div className="p-6 overflow-y-auto custom-scrollbar-dark flex-1">
                                <div className="space-y-6">
                                    <div className="grid grid-cols-2 gap-6">
                                        <div className="col-span-2 text-xs font-black uppercase tracking-widest text-slate-400 border-b border-zinc-900 pb-2">Core Identifiers</div>
                                        <div className="col-span-2">
                                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Scheme Title <span className="text-rose-500">*</span></label>
                                            <input type="text" value={addForm.scheme_name} onChange={e => setAddForm({ ...addForm, scheme_name: e.target.value })} className="w-full bg-white dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="e.g. PM Kisan Samman Nidhi" />
                                        </div>
                                        <div className="col-span-2">
                                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Source URL <span className="text-rose-500">*</span></label>
                                            <input type="text" value={addForm.source_url} onChange={e => setAddForm({ ...addForm, source_url: e.target.value })} className="w-full bg-white dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="e.g. https://pmkisan.gov.in" />
                                        </div>
                                        
                                        <div className="col-span-2 text-xs font-black uppercase tracking-widest text-slate-400 border-b border-zinc-900 pb-2 mt-4">Demographics</div>
                                        <div>
                                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Category (Sector)</label>
                                            <input type="text" value={addForm.scheme_category} onChange={e => setAddForm({ ...addForm, scheme_category: e.target.value })} className="w-full bg-white dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="e.g. Agriculture" />
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">State Applicable</label>
                                            <input type="text" value={addForm.state_applicable} onChange={e => setAddForm({ ...addForm, state_applicable: e.target.value })} className="w-full bg-white dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="e.g. All India" />
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Target Age Min</label>
                                            <input type="number" value={addForm.target_age_min} onChange={e => setAddForm({ ...addForm, target_age_min: e.target.value })} className="w-full bg-white dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="Optional" />
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Target Age Max</label>
                                            <input type="number" value={addForm.target_age_max} onChange={e => setAddForm({ ...addForm, target_age_max: e.target.value })} className="w-full bg-white dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="Optional" />
                                        </div>
                                        <div className="col-span-2">
                                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-2">Income Max Limit (₹)</label>
                                            <input type="number" value={addForm.income_max} onChange={e => setAddForm({ ...addForm, income_max: e.target.value })} className="w-full bg-white dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-lg px-4 py-2.5 text-slate-900 dark:text-white focus:outline-none focus:border-brand-500" placeholder="e.g. 250000" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="px-6 py-5 border-t border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50 flex justify-end gap-3 shrink-0">
                                <button onClick={() => setShowAddModal(false)} className="px-5 py-2.5 text-slate-500 dark:text-zinc-400 font-medium hover:bg-white dark:bg-zinc-900 rounded-lg transition-colors">Cancel</button>
                                <button onClick={handleAddSubmit} disabled={!addForm.scheme_name || !addForm.source_url} className="px-5 py-2.5 bg-brand-500 disabled:opacity-50 text-slate-900 dark:text-white font-medium hover:bg-brand-400 rounded-lg transition-colors">Add Scheme To DB</button>
                            </div>
                        </div>
                    </div>
                )
            }

            {/* Bulk Upload Modal */}
            {
                showBulkModal && (
                    <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                        <div className="bg-[#0a0a0a] border border-slate-300 dark:border-zinc-900 rounded-2xl w-full max-w-lg overflow-hidden shadow-2xl">
                            <div className="px-6 py-5 border-b border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50 flex justify-between items-center">
                                <h3 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight">Bulk Upload Arrays (.json)</h3>
                                <button onClick={() => { setShowBulkModal(false); setBulkStatus(null) }} className="text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:text-white">&times;</button>
                            </div>
                            <div className="p-8 text-center bg-white dark:bg-zinc-950">
                                <div className="border-2 border-dashed border-slate-300 dark:border-zinc-800 rounded-2xl p-8 hover:border-brand-500/50 transition-colors bg-slate-50 dark:bg-zinc-900/20">
                                    <div className="w-12 h-12 rounded-full bg-brand-500/10 text-brand-400 flex items-center justify-center mx-auto mb-4 text-xl">📄</div>
                                    <p className="text-slate-900 dark:text-white font-medium mb-1">Select a formatted JSON array to upload.</p>
                                    <p className="text-slate-500 dark:text-zinc-500 text-sm mb-6">File must contain list arrays matching DB schema rules.</p>
                                    <label className="px-6 py-3 bg-white dark:bg-zinc-900 border border-slate-300 dark:border-zinc-800 text-slate-900 dark:text-white font-medium hover:bg-slate-50 dark:hover:bg-zinc-800 rounded-lg transition-colors cursor-pointer inline-flex shadow-sm">
                                        Browse JSON Data File
                                        <input type="file" accept=".json" className="hidden" onChange={(e) => setBulkFile(e.target.files[0])} />
                                    </label>
                                    {bulkFile && <p className="mt-4 text-emerald-500 font-medium text-sm">Selected: {bulkFile.name}</p>}
                                </div>
                                {bulkStatus && (
                                    <div className="mt-6 p-4 rounded-xl bg-orange-500/10 border border-orange-500/20 text-orange-400 font-medium text-sm">
                                        {bulkStatus}
                                    </div>
                                )}
                            </div>
                            <div className="px-6 py-5 border-t border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50 flex justify-between items-center">
                                <a href="data:text/json;charset=utf-8,%5B%7B%22scheme_name%22%3A%22Sample%20Bot%20Scheme%22%2C%22source_url%22%3A%22http%3A%2F%2Fexample.com%22%7D%5D" download="template.json" className="text-sm text-brand-500 font-medium hover:underline">Download Template</a>
                                <div className="flex gap-3">
                                    <button onClick={() => { setShowBulkModal(false); setBulkStatus(null) }} className="px-5 py-2.5 text-slate-500 dark:text-zinc-400 font-medium hover:bg-white dark:bg-zinc-900 rounded-lg transition-colors">Cancel</button>
                                    <button disabled={!bulkFile} onClick={handleBulkUpload} className="px-5 py-2.5 bg-brand-500 text-slate-900" style={{ color: "black", fontWeight: "bold", opacity: bulkFile ? 1 : 0.5 }}>Import File</button>
                                </div>
                            </div>
                        </div>
                    </div>
                )
            }

        </div >
    );
};

export default SchemeManagement;
