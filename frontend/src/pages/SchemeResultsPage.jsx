import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSchemes } from '../hooks/useSchemes';
import { useProfile } from '../context/ProfileContext';
import { SchemeCard } from '../components/SchemeCard';
import { Search, Filter, SlidersHorizontal } from 'lucide-react';

const SchemeResultsPage = () => {
    const navigate = useNavigate();
    const { evaluateProfile, loading, error } = useSchemes();
    const { getActiveProfile } = useProfile();
    const [results, setResults] = useState([]);
    const activeProfile = getActiveProfile();

    // Filtering states
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedState, setSelectedState] = useState('All');
    const [selectedCategory, setSelectedCategory] = useState('All');

    // Filter extraction sets
    const [availableStates, setAvailableStates] = useState([]);
    const [availableCategories, setAvailableCategories] = useState([]);

    useEffect(() => {
        if (!activeProfile) {
            navigate('/profile');
            return;
        }

        const checkEligibility = async () => {
            // evaluateProfile auto-sanitizes the profile (handles encrypted
            // category, null fields, and snake_case → camelCase mapping)
            const data = await evaluateProfile(activeProfile);
            if (data) {
                // Sort to maximize financial portfolio
                const sorted = data.sort((a, b) => {
                    if (a.status === 'Eligible' && b.status !== 'Eligible') return -1;
                    if (a.status !== 'Eligible' && b.status === 'Eligible') return 1;
                    
                    const valA = a.max_financial_value_inr || 0;
                    const valB = b.max_financial_value_inr || 0;
                    return valB - valA; 
                });
                
                setResults(sorted);

                // Extract unique metadata for filter dropdowns
                const statesSet = new Set(data.map(item => item.state_applicable).filter(Boolean));
                const categorySet = new Set(data.map(item => item.scheme_category).filter(Boolean));
                setAvailableStates(Array.from(statesSet).sort());
                setAvailableCategories(Array.from(categorySet).sort());
            }
        };

        checkEligibility();
    }, [navigate, activeProfile, evaluateProfile]);

    const filteredResults = useMemo(() => {
        return results.filter(scheme => {
            const matchesSearch = scheme.scheme_name.toLowerCase().includes(searchTerm.toLowerCase()) || 
                                  (scheme.scheme_description && scheme.scheme_description.toLowerCase().includes(searchTerm.toLowerCase()));
            const matchesState = selectedState === 'All' || scheme.state_applicable === selectedState;
            const matchesCategory = selectedCategory === 'All' || scheme.scheme_category === selectedCategory;
            return matchesSearch && matchesState && matchesCategory;
        });
    }, [results, searchTerm, selectedState, selectedCategory]);

    if (loading) return (
        <div className="max-w-7xl mx-auto px-4 flex flex-col items-center justify-center min-h-[50vh]">
            <div className="w-10 h-10 border-4 border-orange-600 border-t-transparent rounded-full animate-spin mb-4"></div>
            <div className="text-lg font-bold text-slate-800 dark:text-zinc-50 mb-1">Running Deterministic Evaluation</div>
            <div className="text-slate-500 dark:text-zinc-400 text-sm font-medium">Analyzing your demographic matrix...</div>
        </div>
    );

    if (error) return (
        <div className="max-w-7xl mx-auto px-4 py-8 flex justify-center">
            <div className="pro-card border border-rose-200 p-8 rounded-2xl shadow-2xl shadow-black/5 text-center max-w-md w-full">
                <div className="text-4xl mb-3">⚠️</div>
                <div className="text-slate-700 dark:text-zinc-200 font-bold mb-2 text-lg">Evaluation Error</div>
                <div className="text-rose-600 text-sm font-medium mb-4">{error}</div>
                <p className="text-slate-500 dark:text-zinc-400 text-xs mb-6">
                    This can happen if your profile is incomplete. Please make sure your State and basic details are filled in.
                </p>
                <button
                    onClick={() => navigate('/profile')}
                    className="px-6 py-2.5 bg-orange-600 text-white font-bold rounded-xl text-sm hover:bg-orange-700 transition-colors"
                >
                    Update My Profile
                </button>
            </div>
        </div>
    );

    return (
        <div className="max-w-7xl mx-auto pt-6 px-4 py-8 relative z-10 animate-fade-in w-full pb-12">
            
            {/* Header */}
            <div className="mb-8">
                <h2 className="text-3xl font-black mb-1 text-slate-900 dark:text-white tracking-tight">Eligibility Results</h2>
                <div className="text-slate-500 dark:text-zinc-400 font-medium text-sm">Showing the schemes you are conditionally cleared for.</div>
            </div>

            {/* Filter Hub */}
            <div className="pro-card p-4 sm:p-6 rounded-3xl mb-10 shadow-2xl flex flex-col md:flex-row gap-4 border border-slate-200 dark:border-white/5">
                <div className="flex-1 relative">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                    <input 
                        type="text" 
                        placeholder="Search specific schemes..." 
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/10 rounded-2xl pl-11 pr-4 py-3.5 text-sm font-medium text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-orange-500/50 transition-colors"
                    />
                </div>
                
                <div className="flex flex-col sm:flex-row gap-4 w-full md:w-auto">
                    <div className="relative w-full sm:w-60 shrink-0">
                        <select 
                            value={selectedCategory} 
                            onChange={(e) => setSelectedCategory(e.target.value)}
                            className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/10 rounded-2xl pl-4 pr-10 py-3.5 text-sm font-bold text-slate-700 dark:text-zinc-300 appearance-none focus:outline-none focus:border-orange-500/50 transition-colors cursor-pointer"
                        >
                            <option value="All">All Categories</option>
                            {availableCategories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                        </select>
                        <SlidersHorizontal size={14} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
                    </div>

                    <div className="relative w-full sm:w-60 shrink-0">
                        <select 
                            value={selectedState} 
                            onChange={(e) => setSelectedState(e.target.value)}
                            className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/10 rounded-2xl pl-4 pr-10 py-3.5 text-sm font-bold text-slate-700 dark:text-zinc-300 appearance-none focus:outline-none focus:border-orange-500/50 transition-colors cursor-pointer"
                        >
                            <option value="All">All States (India)</option>
                            {availableStates.map(state => <option key={state} value={state}>{state}</option>)}
                        </select>
                        <Filter size={14} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
                    </div>
                </div>
            </div>

            <div className="space-y-12">
                {/* Fully Eligible Section */}
                {filteredResults.filter(r => r.status === 'Eligible').length > 0 && (
                    <section>
                        <h3 className="text-xl font-black flex items-center gap-3 text-emerald-700 dark:text-emerald-400 mb-6 tracking-tight">
                            <span className="w-3 h-3 rounded-full bg-emerald-500 shadow-[0_0_10px_rgba(16,185,129,0.5)]"></span>
                            Fully Eligible
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {filteredResults
                                .filter(r => r.status === 'Eligible')
                                .map((r) => (
                                    <SchemeCard key={r.scheme_id} scheme={r} showScore={true} hideStatus={true} />
                                ))}
                        </div>
                    </section>
                )}

                {/* Partially Eligible Section */}
                {filteredResults.filter(r => r.status === 'Partially Eligible').length > 0 && (
                    <section>
                        <h3 className="text-xl font-black flex items-center gap-3 text-amber-600 dark:text-amber-500 mb-6 mt-8 border-t border-slate-200 dark:border-white/5 pt-8 tracking-tight">
                            <span className="w-3 h-3 rounded-full bg-amber-500 shadow-[0_0_10px_rgba(245,158,11,0.5)]"></span>
                            Partially Eligible
                            <span className="text-xs text-slate-400 dark:text-zinc-500 font-bold uppercase tracking-widest ml-2 bg-slate-100 dark:bg-white/5 px-2 py-1 rounded">Delta Requirements</span>
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {filteredResults
                                .filter(r => r.status === 'Partially Eligible')
                                .map((r) => (
                                    <SchemeCard key={r.scheme_id} scheme={r} showScore={true} hideStatus={true} />
                                ))}
                        </div>
                    </section>
                )}

                {filteredResults.length === 0 && !loading && (
                    <div className="pro-card border text-center text-slate-500 dark:text-zinc-400 col-span-full py-20 rounded-3xl border-slate-200 dark:border-white/5 shadow-2xl flex flex-col items-center justify-center">
                        <div className="w-16 h-16 rounded-3xl bg-slate-100 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 flex items-center justify-center mb-4">
                            <Filter size={24} className="text-orange-500" />
                        </div>
                        <h4 className="text-slate-900 dark:text-white font-black text-xl mb-2 tracking-tight">No Filter Matches</h4>
                        <p className="text-sm font-medium">Clear your state or category filters to expand the directory.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SchemeResultsPage;
