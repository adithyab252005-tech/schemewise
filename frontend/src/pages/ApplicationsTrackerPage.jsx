import { useState } from 'react';
import { useProfile } from '../context/ProfileContext';
import { CheckSquare, Square, Plus, Trash2, ClipboardList, ChevronDown, ChevronUp } from 'lucide-react';

const STEPS = [
    'Read scheme eligibility criteria',
    'Gather required documents',
    'Fill application form (online / offline)',
    'Submit application',
    'Payment / verification done',
    'Received acknowledgement / receipt',
    'Scheme benefit received',
];

const useLocalApplications = (profileId) => {
    const key = `applications_${profileId || 'guest'}`;
    const [apps, setApps] = useState(() => {
        try {
            const saved = localStorage.getItem(key);
            return saved ? JSON.parse(saved) : [];
        } catch { return []; }
    });

    const save = (updated) => {
        setApps(updated);
        localStorage.setItem(key, JSON.stringify(updated));
    };

    return [apps, save];
};

const ApplicationsTrackerPage = () => {
    const { getActiveProfile } = useProfile();
    const profile = getActiveProfile();
    const [apps, saveApps] = useLocalApplications(profile?.id);

    const [showForm, setShowForm] = useState(false);
    const [newScheme, setNewScheme] = useState('');
    const [expandedId, setExpandedId] = useState(null);

    const handleAdd = (e) => {
        e.preventDefault();
        if (!newScheme.trim()) return;
        const entry = {
            id: Date.now(),
            schemeName: newScheme.trim(),
            addedAt: new Date().toLocaleDateString('en-IN'),
            ticked: [],
        };
        saveApps([entry, ...apps]);
        setNewScheme('');
        setShowForm(false);
        setExpandedId(entry.id);
    };

    const toggleStep = (appId, stepIdx) => {
        const updated = apps.map(a => {
            if (a.id !== appId) return a;
            const ticked = a.ticked.includes(stepIdx)
                ? a.ticked.filter(i => i !== stepIdx)
                : [...a.ticked, stepIdx];
            return { ...a, ticked };
        });
        saveApps(updated);
    };

    const removeApp = (appId) => {
        saveApps(apps.filter(a => a.id !== appId));
        if (expandedId === appId) setExpandedId(null);
    };

    const getProgress = (ticked) => Math.round((ticked.length / STEPS.length) * 100);

    return (
        <div className="flex-1 px-4 sm:px-6 py-8 pb-16 max-w-5xl mx-auto w-full">
            {/* Header */}
            <div className="mb-8 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-2xl sm:text-3xl font-black text-slate-900 dark:text-white tracking-tighter mb-1 flex items-center gap-3">
                        <ClipboardList className="text-orange-500" size={28} /> My Applications
                    </h1>
                    <p className="text-slate-500 dark:text-zinc-400 font-medium text-sm">Track your own scheme application progress step by step.</p>
                </div>
                <button
                    onClick={() => setShowForm(v => !v)}
                    className="flex items-center gap-2 px-5 py-3 bg-orange-600 text-slate-900 dark:text-white font-bold rounded-2xl hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 transition-all shadow-md text-sm shrink-0"
                >
                    <Plus size={18} /> Add Application
                </button>
            </div>

            {/* Add Form */}
            {showForm && (
                <form onSubmit={handleAdd} className="pro-card rounded-2xl p-6 border border-orange-200 shadow-2xl shadow-black/5 dark:shadow-black/40 mb-6 flex flex-col sm:flex-row gap-4 items-end">
                    <div className="flex-1 flex flex-col gap-1.5">
                        <label className="text-sm font-bold text-slate-600 dark:text-zinc-300">Scheme Name</label>
                        <input
                            required
                            autoFocus
                            value={newScheme}
                            onChange={e => setNewScheme(e.target.value)}
                            placeholder="e.g. PM Vishwakarma Yojana, Scholarship for OBC Students…"
                            className="w-full px-4 py-3 bg-transparent border border-slate-200 dark:border-white/5 rounded-xl text-sm text-slate-700 dark:text-zinc-200 focus:ring-2 focus:ring-orange-500/30/20 focus:border-slate-200 dark:border-white/5 outline-none"
                        />
                    </div>
                    <div className="flex gap-2">
                        <button type="button" onClick={() => setShowForm(false)} className="px-5 py-3 rounded-xl border border-slate-200 dark:border-white/5 text-slate-500 dark:text-zinc-400 font-bold text-sm hover:bg-transparent transition-all">Cancel</button>
                        <button type="submit" className="px-5 py-3 bg-orange-600 text-slate-900 dark:text-white font-bold rounded-xl text-sm hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 transition-all shadow">Add</button>
                    </div>
                </form>
            )}

            {/* Empty State */}
            {apps.length === 0 && (
                <div className="pro-card rounded-3xl p-16 text-center border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40">
                    <span className="text-5xl block mb-4">📋</span>
                    <h2 className="text-xl font-black text-slate-900 dark:text-white tracking-tighter mb-2">No applications tracked yet</h2>
                    <p className="text-slate-500 dark:text-zinc-400 text-sm font-medium mb-6">Add a scheme you are applying for and tick off your progress at each step.</p>
                    <button onClick={() => setShowForm(true)} className="px-6 py-3 bg-orange-600 text-slate-900 dark:text-white font-bold rounded-2xl text-sm hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 transition-all shadow">
                        + Add Your First Application
                    </button>
                </div>
            )}

            {/* Application Cards */}
            <div className="flex flex-col gap-5">
                {apps.map(app => {
                    const progress = getProgress(app.ticked);
                    const isExpanded = expandedId === app.id;

                    return (
                        <div key={app.id} className="pro-card rounded-3xl border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 overflow-hidden">
                            {/* Card Header */}
                            <div
                                className="flex items-center justify-between gap-4 px-6 py-5 cursor-pointer hover:bg-transparent transition-colors"
                                onClick={() => setExpandedId(isExpanded ? null : app.id)}
                            >
                                <div className="flex-1 min-w-0">
                                    <p className="text-[11px] font-bold uppercase tracking-widest text-slate-500 dark:text-zinc-400 mb-1">{app.addedAt}</p>
                                    <h2 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight truncate">{app.schemeName}</h2>
                                </div>
                                <div className="flex items-center gap-3 shrink-0">
                                    {/* Progress Badge */}
                                    <span className={`text-xs font-bold px-3 py-1.5 rounded-full ${progress === 100 ? 'bg-emerald-100 text-emerald-700' : progress > 0 ? 'bg-amber-100 text-amber-700' : 'bg-orange-600 text-slate-500 dark:text-zinc-400'}`}>
                                        {progress === 100 ? '✓ Complete' : `${progress}%`}
                                    </span>
                                    <button
                                        onClick={e => { e.stopPropagation(); removeApp(app.id); }}
                                        className="text-slate-500 dark:text-zinc-400 hover:text-rose-500 hover:bg-rose-50 p-2 rounded-xl transition-colors border border-transparent hover:border-rose-100"
                                        title="Remove"
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                    <div className="text-slate-500 dark:text-zinc-400">
                                        {isExpanded ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                                    </div>
                                </div>
                            </div>

                            {/* Progress Bar */}
                            <div className="h-1.5 bg-orange-600 mx-6 rounded-full overflow-hidden">
                                <div
                                    className={`h-full rounded-full transition-all duration-500 ${progress === 100 ? 'bg-emerald-500' : 'bg-orange-600'}`}
                                    style={{ width: `${progress}%` }}
                                />
                            </div>

                            {/* Expandable Checklist */}
                            {isExpanded && (
                                <div className="px-6 py-5 flex flex-col gap-2.5 border-t border-zinc-50 mt-2">
                                    <h3 className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-zinc-400 mb-2">Application Steps</h3>
                                    {STEPS.map((step, idx) => {
                                        const ticked = app.ticked.includes(idx);
                                        return (
                                            <div
                                                key={idx}
                                                onClick={() => toggleStep(app.id, idx)}
                                                className="flex items-center gap-3 cursor-pointer group py-2 px-3 rounded-xl hover:bg-transparent transition-colors"
                                            >
                                                <div className={`shrink-0 ${ticked ? 'text-orange-500' : 'text-slate-600 dark:text-zinc-300 group-hover:text-slate-500 dark:text-zinc-400 transition-colors'}`}>
                                                    {ticked ? <CheckSquare size={20} /> : <Square size={20} />}
                                                </div>
                                                <span className={`text-sm font-medium transition-colors ${ticked ? 'text-slate-500 dark:text-zinc-400 line-through' : 'text-slate-900 dark:text-white group-hover:text-orange-500'}`}>
                                                    {step}
                                                </span>
                                            </div>
                                        );
                                    })}
                                    <p className="text-xs text-slate-500 dark:text-zinc-400 font-medium mt-3 pl-2">
                                        {app.ticked.length}/{STEPS.length} steps completed
                                    </p>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default ApplicationsTrackerPage;
