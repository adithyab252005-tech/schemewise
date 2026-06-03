import { useState } from 'react';
import { useSchemes } from '../hooks/useSchemes';
import { Settings2, Cpu, Check, AlertCircle } from 'lucide-react';

const selectClass = "w-full h-10 px-3 bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg text-[13px] text-slate-800 dark:text-zinc-100 focus:ring-1 focus:ring-orange-500 focus:border-orange-500 outline-none transition-colors appearance-none";
const inputClass = "w-full h-10 px-3 bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg text-[13px] text-slate-800 dark:text-zinc-100 focus:ring-1 focus:ring-orange-500 focus:border-orange-500 outline-none transition-colors placeholder:text-slate-400 dark:placeholder:text-zinc-600";

const Field = ({ label, children }) => (
    <div className="flex flex-col gap-1.5">
        <label className="text-[12px] font-semibold text-slate-500 dark:text-zinc-400 tracking-wide">{label}</label>
        {children}
    </div>
);

const SimulatorPage = () => {
    const { evaluateProfile, loading } = useSchemes();
    const [profile, setProfile] = useState({ 
        age: '', gender: 'Male', state: 'Maharashtra', district: '', city: '',
        occupation: '', income: '', category: 'General', ruralUrban: 'Urban',
        maritalStatus: 'Single', isDifferentlyAbled: 'No', isBPL: 'No',
        isMinority: 'No', isStudent: 'No', studentLevel: ''
    });
    const [results, setResults] = useState(null);

    const handleChange = (e) => setProfile(p => ({ ...p, [e.target.name]: e.target.value }));
    const handleRun = async (e) => { e.preventDefault(); const data = await evaluateProfile(profile); if (data) setResults(data); };

    const eligible = results?.filter(r => r.status === 'Eligible') || [];
    const partial = results?.filter(r => r.status === 'Partially Eligible') || [];

    return (
        <div className="flex-1 max-w-5xl mx-auto w-full px-6 py-10">
            <div className="mb-8 border-b border-slate-200 dark:border-slate-200 dark:border-zinc-800/80 pb-6">
                <h1 className="text-2xl font-semibold text-slate-800 dark:text-zinc-100 tracking-tight flex items-center gap-2 mb-1.5">
                    <Settings2 className="text-orange-500" size={24} />
                    Rules Engine Simulator
                </h1>
                <p className="text-slate-500 dark:text-zinc-400 text-sm">Input raw demographic variables to instantly compute state and central eligibilities.</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                {/* Inputs Column */}
                <div className="lg:col-span-5 flex flex-col gap-6">
                    <div className="pro-card rounded-[2rem] p-8">
                        <form onSubmit={handleRun} className="flex flex-col gap-5">
                            <h3 className="text-[11px] font-bold text-slate-500 dark:text-zinc-500 uppercase tracking-widest border-b border-slate-100 dark:border-white/5 pb-3">Input Parameters</h3>
                            
                            
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <Field label="Age"><input type="number" name="age" value={profile.age} onChange={handleChange} placeholder="Years" className={inputClass} /></Field>
                                <Field label="Gender"><select name="gender" value={profile.gender} onChange={handleChange} className={selectClass}>{['Male', 'Female', 'Other'].map(c => <option key={c}>{c}</option>)}</select></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <Field label="Occupation"><input name="occupation" value={profile.occupation} onChange={handleChange} placeholder="Farmer, Student..." className={inputClass} /></Field>
                                <Field label="Income (₹)"><input type="number" name="income" value={profile.income} onChange={handleChange} placeholder="120000" className={inputClass} /></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <Field label="Category"><select name="category" value={profile.category} onChange={handleChange} className={selectClass}>{['General','OBC','SC','ST','Minority'].map(c => <option key={c}>{c}</option>)}</select></Field>
                                <Field label="Marital Status"><select name="maritalStatus" value={profile.maritalStatus} onChange={handleChange} className={selectClass}>{['Single', 'Married', 'Divorced', 'Widowed'].map(c => <option key={c}>{c}</option>)}</select></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                                <Field label="Area"><select name="ruralUrban" value={profile.ruralUrban} onChange={handleChange} className={selectClass}><option>Urban</option><option>Rural</option></select></Field>
                                <Field label="Minority"><select name="isMinority" value={profile.isMinority} onChange={handleChange} className={selectClass}><option>No</option><option>Yes</option></select></Field>
                                <Field label="BPL Stat"><select name="isBPL" value={profile.isBPL} onChange={handleChange} className={selectClass}><option>No</option><option>Yes</option></select></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <Field label="State"><select name="state" value={profile.state} onChange={handleChange} className={selectClass}><option>Maharashtra</option><option>Tamil Nadu</option><option>Delhi</option><option>ALL</option></select></Field>
                                <Field label="District"><input name="district" value={profile.district} onChange={handleChange} placeholder="Optional" className={inputClass} /></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                                <Field label="Differently Abled"><select name="isDifferentlyAbled" value={profile.isDifferentlyAbled} onChange={handleChange} className={selectClass}><option>No</option><option>Yes</option></select></Field>
                                <Field label="Student"><select name="isStudent" value={profile.isStudent} onChange={handleChange} className={selectClass}><option>No</option><option>Yes</option></select></Field>
                                <Field label="Student Level"><select name="studentLevel" value={profile.studentLevel} onChange={handleChange} className={selectClass}><option>None</option><option>School</option><option>Undergraduate</option><option>Postgraduate</option></select></Field>
                            </div>
                            
                            <button type="submit" disabled={loading} className="w-full mt-4 h-10 bg-zinc-100 hover:bg-white text-zinc-900 font-semibold rounded-lg text-sm transition-colors shadow-[0_0_15px_rgba(255,255,255,0.1)] flex items-center justify-center gap-2 disabled:bg-zinc-800 disabled:text-slate-500 dark:text-zinc-500 disabled:shadow-none">
                                {loading ? <div className="w-4 h-4 border-2 border-zinc-500 border-t-transparent rounded-full animate-spin"></div> : <Cpu size={16} />}
                                {loading ? 'Computing...' : 'Execute Engine'}
                            </button>
                        </form>
                    </div>
                </div>

                {/* Results Column */}
                <div className="lg:col-span-7 flex flex-col min-h-[400px]">
                    {results === null ? (
                        <div className="flex-1 border border-dashed border-slate-200 dark:border-white/10 rounded-3xl flex flex-col items-center justify-center py-16 px-8 bg-slate-50/50 dark:bg-zinc-900/30 text-center gap-4">
                            <div className="w-14 h-14 rounded-2xl bg-white dark:bg-[#11151C] border border-slate-200 dark:border-white/5 flex items-center justify-center shadow-sm">
                                <Cpu size={26} strokeWidth={1.5} className="text-slate-400 dark:text-zinc-500" />
                            </div>
                            <div>
                                <p className="text-sm font-bold text-slate-700 dark:text-zinc-300 mb-1">Rules Engine Ready</p>
                                <p className="text-xs text-slate-400 dark:text-zinc-500 max-w-xs leading-relaxed">
                                    Fill in the demographic parameters on the left and click <strong className="text-[#FF5E00]">Execute Engine</strong> to instantly compute which government schemes match the profile.
                                </p>
                            </div>
                            <div className="flex flex-col gap-2 w-full max-w-xs mt-2">
                                {[
                                    '✓ Tests against 800+ central & state schemes',
                                    '✓ Returns Eligible and Partial matches',
                                    '✓ No login required — sandbox mode',
                                ].map(tip => (
                                    <div key={tip} className="text-[11px] text-left font-medium text-slate-400 dark:text-zinc-500 flex items-center gap-2">
                                        <span>{tip}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ) : (
                        <div className="flex flex-col gap-6 animate-fade-in">
                            {/* Score Row */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="bg-emerald-50 dark:bg-emerald-500/10 border border-emerald-200 dark:border-emerald-500/20 rounded-2xl p-6 flex flex-col shadow-sm">
                                    <span className="text-[10px] font-bold text-emerald-600 dark:text-emerald-500 uppercase tracking-widest mb-1.5">Eligible Hits</span>
                                    <span className="text-4xl font-black text-emerald-700 dark:text-emerald-400">{eligible.length}</span>
                                </div>
                                <div className="bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/20 rounded-2xl p-6 flex flex-col shadow-sm">
                                    <span className="text-[10px] font-bold text-amber-600 dark:text-amber-500 uppercase tracking-widest mb-1.5">Partial Hits</span>
                                    <span className="text-4xl font-black text-amber-700 dark:text-amber-400">{partial.length}</span>
                                </div>
                            </div>

                            {/* Detailed Output */}
                            <div className="pro-card rounded-[2rem] flex flex-col overflow-hidden">
                                <div className="px-6 py-4 border-b border-slate-200/50 dark:border-white/5 bg-slate-50 dark:bg-zinc-900">
                                    <h3 className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Compute Trace</h3>
                                </div>
                                <div className="p-2 flex flex-col gap-1 max-h-[350px] overflow-y-auto custom-scrollbar-dark">
                                    {eligible.map((s, i) => (
                                        <div key={i} className="flex justify-between items-start p-3 hover:bg-slate-200 dark:hover:bg-slate-100 dark:hover:bg-zinc-800/30 rounded-lg transition-colors group">
                                            <div className="flex items-start gap-2 max-w-[80%]">
                                                <Check size={14} className="text-emerald-500 mt-0.5 shrink-0" />
                                                <span className="text-sm font-medium text-slate-600 dark:text-zinc-300 leading-snug group-hover:text-slate-800 dark:text-zinc-100">{s.scheme_name}</span>
                                            </div>
                                            <span className="text-[10px] font-mono text-emerald-500/70 bg-emerald-500/10 px-1.5 py-0.5 rounded border border-emerald-500/20">PASS</span>
                                        </div>
                                    ))}
                                    {partial.map((s, i) => (
                                        <div key={`p-${i}`} className="flex justify-between items-start p-3 hover:bg-slate-200 dark:hover:bg-slate-100 dark:hover:bg-zinc-800/30 rounded-lg transition-colors group">
                                            <div className="flex items-start gap-2 max-w-[80%]">
                                                <AlertCircle size={14} className="text-amber-500 mt-0.5 shrink-0" />
                                                <span className="text-sm font-medium text-slate-500 dark:text-zinc-400 leading-snug group-hover:text-slate-700 dark:text-zinc-200">{s.scheme_name}</span>
                                            </div>
                                            <span className="text-[10px] font-mono text-amber-500/70 bg-amber-500/10 px-1.5 py-0.5 rounded border border-amber-500/20">PARTIAL</span>
                                        </div>
                                    ))}
                                    {results.length === 0 && (
                                        <div className="p-8 text-center text-sm font-medium text-slate-500 dark:text-zinc-500">
                                            Engine returned 0 matches for these parameters.
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SimulatorPage;
