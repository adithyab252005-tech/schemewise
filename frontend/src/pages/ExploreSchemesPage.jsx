import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Filter, RefreshCcw } from 'lucide-react';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import { useSchemes } from '../hooks/useSchemes';
import { useProfile } from '../context/ProfileContext';

const ExploreSchemesPage = () => {
    // Filter States
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedState, setSelectedState] = useState('ALL');
    const [selectedCategory, setSelectedCategory] = useState('ALL');
    const [selectedGender, setSelectedGender] = useState('ALL');
    const [selectedAge, setSelectedAge] = useState('');
    const [selectedCaste, setSelectedCaste] = useState('ALL');
    const [selectedResidence, setSelectedResidence] = useState('ALL');
    const [selectedOccupation, setSelectedOccupation] = useState('ALL');
    const [showOnlyEligible, setShowOnlyEligible] = useState(false);
    const [showMobileFilters, setShowMobileFilters] = useState(false);

    const [selectedBpl, setSelectedBpl] = useState('ALL');

    const { fetchAllSchemes, evaluateProfile, schemes, loading } = useSchemes();
    const { getActiveProfile } = useProfile();
    const [eligibilityMap, setEligibilityMap] = useState({});

    // Category Icon Mapping
    const getCategoryIcon = (cat) => {
        if (!cat) return "🏛️";
        if (cat.includes("Health")) return "🏥";
        if (cat.includes("Education")) return "📚";
        if (cat.includes("Agriculture")) return "🌱";
        if (cat.includes("Business")) return "💼";
        if (cat.includes("Housing")) return "🏠";
        if (cat.includes("Social")) return "🤝";
        return "🏛️";
    };

    useEffect(() => {
        fetchAllSchemes();

        const profile = getActiveProfile();
        if (profile) {
            evaluateProfile(profile).then(results => {
                if (results) {
                    const emap = {};
                    results.forEach(r => {
                        emap[r.scheme_id] = r.score_percentage;
                    });
                    setEligibilityMap(emap);
                }
            });
        }
    }, [fetchAllSchemes, evaluateProfile, getActiveProfile]);

    const filteredSchemes = schemes.filter(s => {
        const name = s.scheme_name || '';
        const matchesSearch = !searchTerm || name.toLowerCase().includes(searchTerm.toLowerCase());

        const schemeState = s.state_applicable || 'ALL';
        const stateMatch = selectedState === 'ALL'
            || schemeState === 'ALL'
            || schemeState === selectedState
            || (selectedState === 'Central' && (s.scheme_type === 'Central' || schemeState === 'ALL'));

        const categoryMatch = selectedCategory === 'ALL'
            || (s.scheme_category || '').toLowerCase().includes(selectedCategory.toLowerCase());

        const schemeGender = (s.target_gender || 'All').toLowerCase();
        const genderMatch = selectedGender === 'ALL'
            || schemeGender === 'all'
            || schemeGender === selectedGender.toLowerCase();

        let ageMatch = true;
        if (selectedAge) {
            const age = parseInt(selectedAge);
            if (!isNaN(age)) {
                if (s.target_age_min != null && age < Number(s.target_age_min)) ageMatch = false;
                if (s.target_age_max != null && age > Number(s.target_age_max)) ageMatch = false;
            }
        }

        const eligCats = s.eligible_categories || [];
        const casteMatch = selectedCaste === 'ALL'
            || eligCats.length === 0
            || eligCats.some(c => c === 'ALL' || c.toLowerCase() === selectedCaste.toLowerCase());

        const schemeRes = (s.rural_urban || 'Both').toLowerCase();
        const resMatch = selectedResidence === 'ALL'
            || schemeRes === 'both'
            || schemeRes === selectedResidence.toLowerCase();

        const occReq = s.occupation_required || [];
        const occMatch = selectedOccupation === 'ALL'
            || occReq.length === 0
            || occReq.some(o => o === 'Any' || o.toLowerCase() === selectedOccupation.toLowerCase());

        const schemeBpl = (s.bpl_required || 'No').toString().toLowerCase();
        const bplMatch = selectedBpl === 'ALL'
            || (selectedBpl === 'Yes' && schemeBpl === 'yes')
            || (selectedBpl === 'No' && schemeBpl !== 'yes');

        const isEligible = eligibilityMap[s.scheme_id] === 100;
        const matchesEligible = showOnlyEligible ? isEligible : true;

        return matchesSearch && stateMatch && categoryMatch && genderMatch && ageMatch && casteMatch && resMatch && occMatch && bplMatch && matchesEligible;
    });

    const resetFilters = () => {
        setSelectedState('ALL');
        setSelectedCategory('ALL');
        setSelectedGender('ALL');
        setSelectedAge('');
        setSelectedCaste('ALL');
        setSelectedResidence('ALL');
        setSelectedOccupation('ALL');
        setSelectedBpl('ALL');
        setShowOnlyEligible(false);
        setSearchTerm('');
    };

    const activeFilterCount = [
        selectedState, selectedCategory, selectedGender, selectedCaste, selectedResidence, selectedOccupation, selectedBpl
    ].filter(v => v !== 'ALL').length + (selectedAge ? 1 : 0);

    return (
        <div className="flex-1 w-full max-w-7xl mx-auto pb-10">
            <div className="mb-6 flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-slate-800 dark:text-zinc-50 mb-1">Explore Directory</h1>
                    <p className="text-sm text-slate-500 dark:text-zinc-400 font-medium">Browse through hundreds of active government welfare programs.</p>
                </div>
            </div>

            <div className="flex flex-col lg:flex-row gap-6 items-start">

                {/* Advanced Filter Sidebar — fixed on desktop, toggleable on mobile */}
                <div className="w-full lg:w-72 shrink-0 pro-card rounded-[2rem] flex flex-col lg:sticky lg:top-8 lg:h-[calc(100vh-9rem)]">
                    {/* Header - always visible */}
                    <div 
                        className="flex justify-between items-center border-b border-slate-100 dark:border-white/5 px-6 py-5 shrink-0 lg:cursor-default cursor-pointer"
                        onClick={() => setShowMobileFilters(!showMobileFilters)}
                    >
                        <h2 className="font-bold text-slate-700 dark:text-zinc-200 flex items-center gap-2">
                            <Filter size={16} className="text-orange-500" />
                            Filters {activeFilterCount > 0 && <span className="bg-orange-100 text-orange-400 text-[10px] px-1.5 py-0.5 rounded-full font-bold">{activeFilterCount}</span>}
                        </h2>
                        <button onClick={(e) => { e.stopPropagation(); resetFilters(); }} className="text-[11px] font-bold text-orange-500 hover:text-orange-300 transition-colors uppercase tracking-wider flex items-center gap-1">
                            <RefreshCcw size={12} /> Reset
                        </button>
                    </div>

                    {/* Scrollable filter list */}
                    <div className={`overflow-y-auto px-5 py-4 space-y-4 flex-1 ${!showMobileFilters ? 'hidden lg:block' : 'block'}`}>

                        {/* State */}
                        <div className="space-y-1.5">
                            <label className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">State</label>
                            <select value={selectedState} onChange={e => setSelectedState(e.target.value)}
                                className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg px-3 py-2 text-sm text-slate-600 dark:text-zinc-300 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium">
                                <option value="ALL">All States / Central</option>
                                <option value="Central">Central Schemes Only</option>
                                <option disabled>── State Schemes ──</option>
                                <option value="Andhra Pradesh">Andhra Pradesh</option>
                                <option value="Arunachal Pradesh">Arunachal Pradesh</option>
                                <option value="Assam">Assam</option>
                                <option value="Bihar">Bihar</option>
                                <option value="Chhattisgarh">Chhattisgarh</option>
                                <option value="Goa">Goa</option>
                                <option value="Gujarat">Gujarat</option>
                                <option value="Haryana">Haryana</option>
                                <option value="Himachal Pradesh">Himachal Pradesh</option>
                                <option value="Jharkhand">Jharkhand</option>
                                <option value="Karnataka">Karnataka</option>
                                <option value="Kerala">Kerala</option>
                                <option value="Madhya Pradesh">Madhya Pradesh</option>
                                <option value="Maharashtra">Maharashtra</option>
                                <option value="Manipur">Manipur</option>
                                <option value="Meghalaya">Meghalaya</option>
                                <option value="Mizoram">Mizoram</option>
                                <option value="Nagaland">Nagaland</option>
                                <option value="Odisha">Odisha</option>
                                <option value="Punjab">Punjab</option>
                                <option value="Rajasthan">Rajasthan</option>
                                <option value="Sikkim">Sikkim</option>
                                <option value="Tamil Nadu">Tamil Nadu</option>
                                <option value="Telangana">Telangana</option>
                                <option value="Tripura">Tripura</option>
                                <option value="Uttar Pradesh">Uttar Pradesh</option>
                                <option value="Uttarakhand">Uttarakhand</option>
                                <option value="West Bengal">West Bengal</option>
                                <option value="Delhi">Delhi</option>
                                <option value="Jammu & Kashmir">Jammu & Kashmir</option>
                                <option value="Ladakh">Ladakh</option>
                                <option value="Puducherry">Puducherry</option>
                            </select>
                        </div>

                        {/* Category */}
                        <div className="space-y-1.5">
                            <label className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Category</label>
                            <select value={selectedCategory} onChange={e => setSelectedCategory(e.target.value)}
                                className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg px-3 py-2 text-sm text-slate-600 dark:text-zinc-300 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium">
                                <option value="ALL">All Categories</option>
                                <option value="Agriculture">Agriculture, Rural & Environment</option>
                                <option value="Business">Business & Entrepreneurship</option>
                                <option value="Education">Education & Learning</option>
                                <option value="Health">Health & Wellness</option>
                                <option value="Housing">Housing & Shelter</option>
                                <option value="Social">Social Welfare & Empowerment</option>
                                <option value="Science">Science & Technology</option>
                                <option value="Sports">Sports & Culture</option>
                                <option value="Women">Women & Child</option>
                            </select>
                        </div>

                        {/* Gender */}
                        <div className="space-y-1.5">
                            <label className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Gender</label>
                            <select value={selectedGender} onChange={e => setSelectedGender(e.target.value)}
                                className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg px-3 py-2 text-sm text-slate-600 dark:text-zinc-300 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium">
                                <option value="ALL">All Genders</option>
                                <option value="Male">Male</option>
                                <option value="Female">Female</option>
                                <option value="Transgender">Transgender</option>
                            </select>
                        </div>

                        {/* Age */}
                        <div className="space-y-1.5">
                            <label className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Age (Years)</label>
                            <input type="number" min="0" max="120"
                                placeholder="e.g. 22"
                                value={selectedAge}
                                onChange={e => setSelectedAge(e.target.value)}
                                className="w-full bg-slate-50/50 dark:bg-zinc-900/30 border border-slate-200 dark:border-white/5 rounded-lg px-3 py-2 text-sm text-slate-600 dark:text-zinc-300 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium"
                            />
                            {selectedAge && <p className="text-[10px] text-slate-500 dark:text-zinc-400">Showing schemes where age {selectedAge} is in the eligible range</p>}
                        </div>

                        {/* Caste / Category */}
                        <div className="space-y-1.5">
                            <label className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Caste / Category</label>
                            <select value={selectedCaste} onChange={e => setSelectedCaste(e.target.value)}
                                className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg px-3 py-2 text-sm text-slate-600 dark:text-zinc-300 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium">
                                <option value="ALL">All Categories</option>
                                <option value="General">General</option>
                                <option value="OBC">OBC</option>
                                <option value="SC">SC (Scheduled Caste)</option>
                                <option value="ST">ST (Scheduled Tribe)</option>
                                <option value="EWS">EWS</option>
                                <option value="Minority">Minority</option>
                            </select>
                        </div>

                        {/* Residence */}
                        <div className="space-y-1.5">
                            <label className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Residence Type</label>
                            <select value={selectedResidence} onChange={e => setSelectedResidence(e.target.value)}
                                className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg px-3 py-2 text-sm text-slate-600 dark:text-zinc-300 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium">
                                <option value="ALL">Rural & Urban</option>
                                <option value="Rural">Rural Only</option>
                                <option value="Urban">Urban Only</option>
                            </select>
                        </div>

                        {/* Occupation */}
                        <div className="space-y-1.5">
                            <label className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Occupation</label>
                            <select value={selectedOccupation} onChange={e => setSelectedOccupation(e.target.value)}
                                className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg px-3 py-2 text-sm text-slate-600 dark:text-zinc-300 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium">
                                <option value="ALL">All Occupations</option>
                                <option value="Student">Student</option>
                                <option value="Farmer">Farmer</option>
                                <option value="Business Owner">Business Owner</option>
                                <option value="Self Employed">Self Employed</option>
                                <option value="Employee">Salaried Employee</option>
                                <option value="Daily Wage Worker">Daily Wage Worker</option>
                                <option value="Unemployed">Unemployed</option>
                            </select>
                        </div>

                        {/* BPL */}
                        <div className="space-y-1.5">
                            <label className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">BPL Status</label>
                            <select value={selectedBpl} onChange={e => setSelectedBpl(e.target.value)}
                                className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/5 rounded-lg px-3 py-2 text-sm text-slate-600 dark:text-zinc-300 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium">
                                <option value="ALL">All Schemes</option>
                                <option value="Yes">BPL Cardholders Only</option>
                                <option value="No">Non-BPL Schemes</option>
                            </select>
                        </div>

                        <div className="pb-2" />
                    </div>
                </div>

                {/* Main Results Area */}
                <div className="flex-1 w-full flex flex-col gap-5">
                    
                    {/* Search & Status Bar */}
                    <div className="flex flex-col md:flex-row gap-4 items-center mb-2">
                        <div className="flex-1 w-full relative h-[52px] bg-white dark:bg-[#09090b] border border-slate-200 dark:border-white/10 rounded-[100px] overflow-hidden group shadow-sm focus-within:ring-2 focus-within:ring-orange-500/50 focus-within:border-orange-500 transition-all duration-300">
                            <div className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-400 dark:text-zinc-500 group-focus-within:text-orange-500 transition-colors">
                                <Search size={20} strokeWidth={2.5} />
                            </div>
                            <input
                                type="text"
                                placeholder="Search schemes, grants, keywords..."
                                className="w-full h-full pl-14 pr-6 bg-transparent outline-none text-[15px] font-medium text-slate-800 dark:text-zinc-100 placeholder:text-slate-400 dark:text-zinc-500"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </div>
                        <div className="shrink-0 flex items-center justify-between md:justify-end gap-4 w-full md:w-auto px-2">
                            <div className="text-sm font-medium text-slate-500 dark:text-zinc-400">
                                <span className="text-slate-800 dark:text-zinc-50 font-black tracking-tight mr-1">{filteredSchemes.length}</span>
                                results
                            </div>
                            <Button
                                variant={showOnlyEligible ? 'primary' : 'outline'}
                                className={`whitespace-nowrap flex items-center gap-2 h-10 px-5 rounded-xl text-sm transition-all duration-300 ${!showOnlyEligible ? 'bg-slate-50 dark:bg-[#09090b] border border-slate-200 dark:border-white/5 text-slate-600 dark:text-zinc-300 hover:bg-slate-100 dark:hover:bg-zinc-900 shadow-sm' : 'bg-orange-600 text-slate-900 dark:text-white shadow-lg'}`}
                                onClick={() => setShowOnlyEligible(!showOnlyEligible)}
                            >
                                <span className={`w-2 h-2 rounded-full ${showOnlyEligible ? 'bg-white shadow-[0_0_8px_rgba(255,255,255,0.8)]' : 'bg-emerald-500 opacity-50'}`}></span>
                                Just For Me
                            </Button>
                        </div>
                    </div>

                    {loading && schemes.length === 0 ? (
                        <div className="flex justify-center py-20 relative z-10">
                            <div className="w-8 h-8 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
                        </div>
                    ) : (
                        <div className="flex flex-col gap-4">
                            {filteredSchemes.map(scheme => {
                                const isEligible = eligibilityMap[scheme.scheme_id] === 100;
                                const isPartial = eligibilityMap[scheme.scheme_id] > 0 && eligibilityMap[scheme.scheme_id] < 100;

                                return (
                                    <div key={scheme.scheme_id} className="pro-card rounded-[2rem] hover:-translate-y-1 transition-transform duration-300 shadow-sm flex flex-col p-6 lg:p-8 group overflow-hidden relative">
                                        <div className="absolute top-0 right-0 w-64 h-64 bg-slate-400/5 dark:bg-white/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2 group-hover:scale-110 transition-transform duration-500 pointer-events-none" />
                                        
                                        <div className="flex justify-between items-start mb-4">
                                            <div className="pr-4 flex-1">
                                                <div className="flex flex-wrap items-center gap-2 mb-3">
                                                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-orange-50 dark:bg-orange-500/10 text-orange-600 dark:text-orange-400 text-[10px] font-extrabold uppercase tracking-widest border border-orange-200/50 dark:border-orange-500/20">
                                                        <span>{getCategoryIcon(scheme.scheme_category)}</span>
                                                        {scheme.scheme_category || "General"}
                                                    </span>
                                                    {(scheme.scheme_type === 'Central' || scheme.state_applicable === 'ALL') && (
                                                        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100 dark:bg-zinc-800/50 text-slate-600 dark:text-zinc-300 text-[10px] font-extrabold uppercase tracking-widest border border-slate-200 dark:border-white/5">
                                                            Central Govt
                                                        </span>
                                                    )}
                                                </div>
                                                <h3 className="text-xl font-black text-black dark:text-white mb-1.5 leading-snug group-hover:text-orange-500 transition-colors">{scheme.scheme_name}</h3>
                                                <p className="text-xs font-semibold text-slate-500 dark:text-zinc-400">Scheme ID: {scheme.scheme_id}</p>
                                            </div>

                                            {isEligible && (
                                                <span className="shrink-0 bg-emerald-100 text-emerald-700 text-[10px] font-bold px-2.5 py-1 rounded-full border border-emerald-200 flex items-center gap-1">
                                                    <div className="w-1.5 h-1.5 rounded-full bg-emerald-500"></div>
                                                    ELIGIBLE
                                                </span>
                                            )}
                                        </div>

                                        <p className="text-slate-500 dark:text-zinc-400 text-sm mb-5 line-clamp-2 leading-relaxed font-medium">
                                            Official government program. {scheme.state_applicable !== 'ALL' ? `Targeted at residents of ${scheme.state_applicable}.` : 'Central government scheme.'} Last updated on {scheme.last_updated ? new Date(scheme.last_updated).toLocaleDateString() : 'N/A'}.
                                        </p>

                                        <div className="flex items-center justify-between border-t border-slate-200 dark:border-white/5 pt-4 mt-auto">
                                            <div className="text-[11px] font-bold text-slate-500 dark:text-zinc-400 tracking-wider uppercase flex items-center gap-2">
                                                <span className={`w-2 h-2 rounded-full ${scheme.status === 'Active' ? 'bg-emerald-500' : 'bg-amber-500'}`}></span>
                                                {scheme.status}
                                            </div>
                                            <Link to={`/scheme/${scheme.scheme_id}`} state={{ resultData: scheme }}>
                                                <Button size="sm" className="bg-slate-50 dark:bg-zinc-900 text-slate-900 dark:text-white hover:bg-slate-100 dark:hover:bg-zinc-800 font-bold border border-slate-200 dark:border-white/5 h-9 px-5 text-xs rounded-xl shadow-sm transition-all duration-300">
                                                    View Details
                                                </Button>
                                            </Link>
                                        </div>
                                    </div>
                                );
                            })}
                            
                            {filteredSchemes.length === 0 && (
                                <div className="text-center py-20 pro-card rounded-2xl shadow-2xl shadow-black/5 dark:shadow-black/40 flex flex-col items-center justify-center">
                                    <div className="w-16 h-16 bg-transparent rounded-full flex items-center justify-center mb-4">
                                        <Search size={24} className="text-slate-500 dark:text-zinc-400" />
                                    </div>
                                    <p className="text-lg font-bold text-slate-700 dark:text-zinc-200 mb-1">No schemes found</p>
                                    <p className="text-sm font-medium text-slate-500 dark:text-zinc-400 max-w-sm mb-6">Try adjusting your sidebar criteria, removing search terms, or disabling "Just For Me".</p>
                                    <Button onClick={resetFilters} className="bg-orange-600 hover:bg-zinc-200 text-slate-600 dark:text-zinc-300 border-none font-bold rounded-xl h-10 px-6">
                                        Reset All Filters
                                    </Button>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ExploreSchemesPage;
