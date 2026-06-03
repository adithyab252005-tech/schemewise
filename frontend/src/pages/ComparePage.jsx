import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSchemes } from '../hooks/useSchemes';
import { useProfile } from '../context/ProfileContext';
import { Search, Loader2, ArrowLeft, Sparkles, CheckCircle } from 'lucide-react';

const COMPARE_ATTRS = [
    { label: 'Scope',           key: s => s.scheme_type || 'Central',                     winFn: null },
    { label: 'Eligible Area',   key: s => s.rural_urban || 'Both',                         winFn: null },
    { label: 'Gender',          key: s => s.target_gender || 'All',                        winFn: null },
    { label: 'Min Age',         key: s => s.target_age_min ? `${s.target_age_min} yrs` : 'None', winFn: (a, b) => (a.target_age_min ?? 0) <= (b.target_age_min ?? 0) },
    { label: 'Max Age',         key: s => s.target_age_max ? `${s.target_age_max} yrs` : 'No limit', winFn: null },
    { label: 'Income Limit',    key: s => s.income_max ? `₹${(+s.income_max).toLocaleString('en-IN')}` : 'No limit', winFn: (a, b) => (a.income_max ?? Infinity) >= (b.income_max ?? Infinity) },
    { label: 'Social Groups',   key: s => Array.isArray(s.eligible_categories) ? s.eligible_categories.join(', ') : (s.eligible_categories || 'All'), winFn: null },
    { label: 'Occupation',      key: s => Array.isArray(s.occupation_required) && s.occupation_required.length ? s.occupation_required.join(', ') : 'Any', winFn: null },
    { label: 'Applied State',   key: s => s.state_applicable === 'ALL' ? 'Pan India' : (s.state_applicable || 'ALL'), winFn: null },
];



const SearchableSelect = ({ label, value, onChange, schemes, placeholder }) => {
    const [search, setSearch] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const wrapperRef = React.useRef(null);
    
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        if (isOpen) document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [isOpen]);

    const filtered = schemes.filter(s => 
        s.scheme_name?.toLowerCase().includes(search.toLowerCase())
    ).slice(0, 20);

    const selectedScheme = schemes.find(s => s.scheme_id?.toString() === value?.toString());

    return (
        <div ref={wrapperRef} className={`relative ${isOpen ? 'z-50' : 'z-10'}`}>
            <label className="text-[10px] font-black text-slate-400 dark:text-zinc-500 uppercase tracking-widest mb-2 block">{label}</label>
            <div 
                onClick={() => setIsOpen(!isOpen)}
                className="w-full pro-card flex items-center justify-between px-4 py-3 cursor-pointer hover:border-orange-500/30 transition-all rounded-xl"
            >
                <span className={`text-sm font-medium truncate ${selectedScheme ? 'text-slate-900 dark:text-white' : 'text-slate-400'}`}>
                    {selectedScheme ? selectedScheme.scheme_name : placeholder}
                </span>
                <Search size={14} className="text-slate-400" />
            </div>

            {isOpen && (
                <div className="absolute z-50 w-full mt-2 pro-card overflow-hidden shadow-2xl animate-in fade-in slide-in-from-top-2 duration-200">
                    <input 
                        autoFocus
                        type="text"
                        placeholder="Type to search..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="w-full bg-slate-50 dark:bg-zinc-900/80 border-b border-slate-100 dark:border-white/5 px-4 py-3 text-sm outline-none text-slate-900 dark:text-white"
                        onClick={(e) => e.stopPropagation()}
                    />
                    <div className="max-h-60 overflow-y-auto bg-white dark:bg-zinc-900">
                        {filtered.length > 0 ? (
                            filtered.map(s => (
                                <div 
                                    key={s.scheme_id}
                                    onClick={() => {
                                        onChange(s.scheme_id.toString());
                                        setIsOpen(false);
                                        setSearch('');
                                    }}
                                    className="px-4 py-3 text-sm hover:bg-orange-500/10 hover:text-orange-500 cursor-pointer transition-colors border-b border-slate-50 dark:border-white/5 last:border-0 text-slate-700 dark:text-zinc-300"
                                >
                                    {s.scheme_name}
                                </div>
                            ))
                        ) : (
                            <div className="px-4 py-8 text-center text-xs text-slate-400 italic">No matches found</div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

const ComparePage = () => {
    const navigate = useNavigate();
    const { getActiveProfile } = useProfile();
    const { fetchAllSchemes, compareSchemes, loading: fetchLoading } = useSchemes();
    const profile = getActiveProfile();
    const [schemes, setSchemes] = useState([]);
    const [s1Id, setS1Id] = useState('');
    const [s2Id, setS2Id] = useState('');
    const [aiVerdict, setAiVerdict] = useState(null);
    const [comparing, setComparing] = useState(false);

    useEffect(() => {
        fetchAllSchemes().then(d => setSchemes(d || []));
    }, [fetchAllSchemes]);

    const handleCompare = async () => {
        if (!s1Id || !s2Id) return;
        setComparing(true);
        const result = await compareSchemes(s1Id, s2Id, profile);
        if (result) setAiVerdict(result.verdict);
        setComparing(false);
    };

    const s1 = schemes.find(s => s.scheme_id?.toString() === s1Id);
    const s2 = schemes.find(s => s.scheme_id?.toString() === s2Id);

    return (
        <div className="flex-1 w-full max-w-4xl mx-auto py-6 px-4 pb-20">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-2 text-xs font-bold text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-white uppercase tracking-widest transition-colors mb-6"
            >
                <ArrowLeft size={16} /> Back
            </button>

            <div className="mb-8">
                <h1 className="text-3xl font-black text-slate-900 dark:text-white tracking-tight">Compare Schemes</h1>
                <p className="text-slate-500 dark:text-zinc-400 text-sm mt-1">Get AI-driven stacking insights & personalized verdicts</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-8">
                <SearchableSelect 
                    label="Scheme A" 
                    value={s1Id} 
                    onChange={setS1Id} 
                    schemes={schemes} 
                    placeholder="Search first scheme..."
                />
                <SearchableSelect 
                    label="Scheme B" 
                    value={s2Id} 
                    onChange={setS2Id} 
                    schemes={schemes} 
                    placeholder="Search second scheme..."
                />
            </div>

            <button 
                onClick={handleCompare}
                disabled={!s1Id || !s2Id || comparing}
                className="w-full mb-10 py-4 bg-orange-600 hover:bg-orange-500 disabled:bg-slate-200 dark:disabled:bg-white/5 text-white font-black rounded-2xl shadow-xl shadow-orange-600/20 transition-all flex items-center justify-center gap-2"
            >
                {comparing ? <><Loader2 size={18} className="animate-spin" /> Analyzing Stacking Rules...</> : 'Compare & Get AI Verdict'}
            </button>

            {aiVerdict && (
                <div className="relative rounded-[2rem] overflow-hidden mb-8 border border-orange-500/20 animate-in fade-in zoom-in-95 duration-500">
                    <div className="h-1.5 w-full bg-gradient-to-r from-orange-600 via-rose-500 to-amber-400" />
                    <div className="bg-[#0D0D1A] p-6 sm:p-10">
                        <div className="flex items-center gap-3 mb-6">
                            <div className="w-12 h-12 rounded-2xl bg-orange-500/20 flex items-center justify-center shrink-0">
                                <Sparkles size={24} className="text-orange-400" />
                            </div>
                            <div>
                                <p className="text-orange-400 text-[10px] font-black tracking-[0.2em] uppercase">Deep Stacking Analysis</p>
                                <p className="text-zinc-500 text-xs">Based on your specific demographic profile</p>
                            </div>
                        </div>
                        
                        <div className="text-white text-sm leading-relaxed whitespace-pre-wrap font-medium">
                            {aiVerdict}
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-8 pt-8 border-t border-white/5">
                            <div className="rounded-2xl p-4 bg-white/5 border border-orange-500/20">
                                <p className="text-orange-500 text-[10px] font-black uppercase tracking-widest mb-1">Scheme A</p>
                                <p className="text-white text-xs font-bold">{s1?.scheme_name}</p>
                            </div>
                            <div className="rounded-2xl p-4 bg-white/5 border border-violet-500/20">
                                <p className="text-violet-500 text-[10px] font-black uppercase tracking-widest mb-1">Scheme B</p>
                                <p className="text-white text-xs font-bold">{s2?.scheme_name}</p>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {s1 && s2 && (
                <div className="pro-card rounded-[2rem] overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-500">
                    <div className="grid grid-cols-[1.5fr_1fr_1fr] gap-0 bg-slate-50 dark:bg-zinc-900/60 border-b border-slate-100 dark:border-white/5 px-6 py-4">
                        <span className="text-[10px] font-black text-slate-400 dark:text-zinc-500 uppercase tracking-widest">Structural Comparison</span>
                        <span className="text-[10px] font-black text-orange-500 uppercase tracking-widest truncate pl-2">{s1.scheme_name.split(' ').slice(0,2).join(' ')}</span>
                        <span className="text-[10px] font-black text-violet-500 uppercase tracking-widest truncate pl-2">{s2.scheme_name.split(' ').slice(0,2).join(' ')}</span>
                    </div>

                    {COMPARE_ATTRS.map(({ label, key, winFn }, idx) => {
                        const v1 = key(s1);
                        const v2 = key(s2);
                        const aWins = winFn ? winFn(s1, s2) : null;
                        return (
                            <div key={idx} className={`grid grid-cols-[1.5fr_1fr_1fr] gap-0 px-6 py-4 items-center ${idx % 2 === 0 ? '' : 'bg-slate-50/50 dark:bg-zinc-900/30'} border-b border-slate-100 dark:border-white/5 last:border-0`}>
                                <span className="text-[10px] font-bold text-slate-400 dark:text-zinc-500 uppercase tracking-wider">{label}</span>
                                <ValueCell value={v1} isWin={aWins === true} color="orange" />
                                <ValueCell value={v2} isWin={aWins === false} color="violet" />
                            </div>
                        );
                    })}
                </div>
            )}

            {!s1 && !s2 && !fetchLoading && (
                <div className="text-center py-24 text-slate-400 dark:text-zinc-500">
                    <div className="w-20 h-20 bg-slate-100 dark:bg-white/5 rounded-3xl flex items-center justify-center mx-auto mb-6">
                        <Sparkles size={32} className="opacity-30" />
                    </div>
                    <p className="font-black text-lg text-slate-600 dark:text-zinc-400">Select schemes to begin</p>
                    <p className="text-sm mt-1 max-w-xs mx-auto">We'll check if you can apply for both and which gives better benefits.</p>
                </div>
            )}
        </div>
    );
};

const ValueCell = ({ value, isWin, color }) => (
    <div className={`flex items-center gap-1.5 px-3 py-2 rounded-xl transition-all ${
        isWin
            ? color === 'orange'
                ? 'bg-orange-50 dark:bg-orange-500/10 ring-1 ring-orange-500/20'
                : 'bg-violet-50 dark:bg-violet-500/10 ring-1 ring-violet-500/20'
            : ''
    }`}>
        {isWin && (
            <CheckCircle size={11} className={color === 'orange' ? 'text-orange-500 shrink-0' : 'text-violet-500 shrink-0'} />
        )}
        <span className={`text-xs font-medium truncate ${
            isWin
                ? color === 'orange' ? 'text-orange-600 dark:text-orange-400 font-bold' : 'text-violet-600 dark:text-violet-400 font-bold'
                : 'text-slate-600 dark:text-zinc-400'
        }`}>{value}</span>
    </div>
);

export default ComparePage;

