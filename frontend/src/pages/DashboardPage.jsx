import { useState, useEffect } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { CheckCircle, Bookmark, Bell, Compass, Play, Sparkles, MoveRight, HelpCircle, FileCheck, MapPin, GraduationCap, QrCode } from 'lucide-react';
import { useProfile } from '../context/ProfileContext';
import { useSchemes } from '../hooks/useSchemes';
import OnboardingTour from '../components/OnboardingTour';

const DashboardPage = () => {
    const { getActiveProfile } = useProfile();
    const { evaluateProfile, fetchUpdates, fetchSavedSchemes } = useSchemes();
    const navigate = useNavigate();
    const profile = getActiveProfile();
    
    const [loading, setLoading] = useState(true);
    const [eligibleCount, setEligibleCount] = useState(0);
    const [topPicks, setTopPicks] = useState([]);
    const [updatesCount, setUpdatesCount] = useState(0);
    const [savedCount, setSavedCount] = useState(0);
    const [civicScore, setCivicScore] = useState(0);
    const [profileCompletion, setProfileCompletion] = useState(0);

    if (profile?.isAdmin) return <Navigate to="/admin" replace />;

    useEffect(() => {
        if (!profile) { setLoading(false); return; }

        // Calculate profile completion
        const fields = ['name', 'dob', 'age', 'gender', 'income', 'occupation', 'state', 'ruralUrban', 'category'];
        const filled = fields.filter(f => profile[f] && profile[f] !== '').length;
        setProfileCompletion(Math.round((filled / fields.length) * 100));

        const fetchData = async () => {
            try {
                const [results, updates, saved] = await Promise.all([
                    evaluateProfile(profile),
                    fetchUpdates(),
                    profile.id ? fetchSavedSchemes(profile.id) : Promise.resolve([]),
                ]);

                setUpdatesCount(updates?.length || 0);
                setSavedCount(saved?.length || 0);

                if (results) {
                    const partial = results.filter(s => s.status === 'Eligible' || s.status === 'Partially Eligible');
                    const eligible = results.filter(s => s.status === 'Eligible');
                    setEligibleCount(eligible.length);
                    // Civic score = what % of total evaluated schemes you are eligible for
                    setCivicScore(results.length > 0 ? Math.round((eligible.length / results.length) * 100) : 0);
                    if (partial.length > 0) {
                        const sorted = [...partial].sort((a, b) => (b.score_percentage || 0) - (a.score_percentage || 0));
                        setTopPicks(sorted.slice(0, 4));
                    }
                }
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [profile?.id]);

    const firstName = profile?.name?.split(' ')[0] || 'User';

    const stats = [
        { 
            label: 'Eligible Schemes', 
            val: eligibleCount, 
            icon: CheckCircle, 
            link: '/results', 
            color: 'text-emerald-500',
            bg: 'bg-emerald-50 dark:bg-emerald-500/10',
            border: 'border-emerald-100 dark:border-emerald-500/20',
            description: 'Active, qualifying schemes'
        },
        { 
            label: 'Saved Schemes', 
            val: savedCount, 
            icon: Bookmark, 
            link: '/saved', 
            color: 'text-violet-500',
            bg: 'bg-violet-50 dark:bg-violet-500/10',
            border: 'border-violet-100 dark:border-violet-500/20',
            description: 'Bookmarked for review'
        },
        { 
            label: 'Scheme Updates', 
            val: updatesCount, 
            icon: Bell, 
            link: '/updates', 
            color: 'text-amber-500',
            bg: 'bg-amber-50 dark:bg-amber-500/10',
            border: 'border-amber-100 dark:border-amber-500/20',
            description: 'New policy notifications'
        },
    ];

    return (
        <div className="flex flex-col gap-5 max-w-7xl mx-auto pb-12 px-2 md:px-0 pt-2 min-h-full animate-fade-in relative z-10">
            <OnboardingTour />
            {/* Missing Location Nag Banner */}
            {(!profile?.district || !profile?.city) && (
                <div className="bg-[#FF5E00] text-white rounded-2xl p-4 sm:p-5 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 mb-1 shadow-lg shadow-orange-500/20 animate-fade-in">
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 bg-white/20 backdrop-blur-md rounded-xl flex items-center justify-center shrink-0">
                            <MapPin size={24} className="text-white drop-shadow-md" />
                        </div>
                        <div>
                            <h4 className="font-extrabold text-[#0B0E14] dark:text-zinc-900 tracking-tight text-lg leading-none mb-1">Location Update Required</h4>
                            <p className="text-sm font-medium text-orange-100 max-w-lg">We need your District and City to accurately map you to nearby E-Seva Kendras and local civic offices.</p>
                        </div>
                    </div>
                    <Link to="/profile" className="px-5 py-3 bg-white hover:bg-slate-50 text-[#FF5E00] font-black text-sm uppercase tracking-widest rounded-xl shadow-md transition-colors shrink-0 w-full md:w-auto text-center border-2 border-transparent">
                        Update Profile
                    </Link>
                </div>
            )}

            {/* Student Details Incomplete Banner */}
            {(profile?.isStudent === 'Yes' || profile?.is_student === 'Yes') &&
             !(profile?.studentLevel || profile?.student_level) && (
                <div className="bg-blue-600 text-white rounded-2xl p-4 sm:p-5 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 mb-1 shadow-lg shadow-blue-500/20 animate-fade-in">
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 bg-white/20 backdrop-blur-md rounded-xl flex items-center justify-center shrink-0">
                            <GraduationCap size={24} className="text-white drop-shadow-md" />
                        </div>
                        <div>
                            <h4 className="font-extrabold text-white tracking-tight text-lg leading-none mb-1">Complete Your Student Profile</h4>
                            <p className="text-sm font-medium text-blue-100 max-w-lg">
                                You're marked as a student but haven't told us your level, grade, or course. Without this, scholarship eligibility is inaccurate.
                            </p>
                        </div>
                    </div>
                    <Link to="/profile" className="px-5 py-3 bg-white hover:bg-slate-50 text-blue-600 font-black text-sm uppercase tracking-widest rounded-xl shadow-md transition-colors shrink-0 w-full md:w-auto text-center border-2 border-transparent">
                        Update Now
                    </Link>
                </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
                
                {/* HERO BENTO */}
                <div className="lg:col-span-8 pro-card p-7 sm:p-9 flex flex-col justify-between relative overflow-hidden group min-h-[280px]">
                    {/* Background Asset */}
                    <img src="/dashboard_hero_abstract.png" alt="Intelligence Core" 
                        className="absolute inset-0 w-full h-full object-cover opacity-[0.12] dark:opacity-[0.30] mix-blend-luminosity dark:mix-blend-overlay group-hover:scale-105 transition-transform duration-[4s] ease-out pointer-events-none" />
                    <div className="absolute inset-0 bg-gradient-to-r from-white dark:from-[#0B0E14] via-white/80 dark:via-[#0B0E14]/80 to-transparent pointer-events-none" />

                    {/* Ambient pulse orb */}
                    <div className="absolute top-0 right-0 w-72 h-72 bg-[#FF5E00]/8 dark:bg-[#FF5E00]/15 blur-[80px] rounded-full pointer-events-none transition-all duration-1000 group-hover:opacity-100 group-hover:w-80 group-hover:h-80 opacity-50" />
                    
                    <div className="relative z-10">
                        <div className="flex items-center gap-2.5 mb-5">
                            <div className="px-3 py-1.5 rounded-lg bg-[#FF5E00]/10 border border-[#FF5E00]/25 flex items-center gap-2">
                                <span className="flex h-1.5 w-1.5 relative">
                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#FF5E00] opacity-75" />
                                    <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-[#FF5E00]" />
                                </span>
                                <span className="text-[9px] font-black text-[#FF5E00] uppercase tracking-widest">Intelligence Core · Live</span>
                            </div>
                        </div>
                        
                        <h1 className="text-3xl sm:text-5xl font-black tracking-tighter leading-tight mb-3 text-slate-900 dark:text-white">
                            Welcome back,<br className="hidden sm:block"/>
                            <span className="text-slate-900 dark:text-transparent dark:bg-clip-text dark:bg-gradient-to-br dark:from-white dark:to-zinc-400">
                                {firstName}.
                            </span>
                        </h1>
                        <p className="font-medium text-slate-500 dark:text-zinc-400 text-sm sm:text-base max-w-lg mb-7 leading-relaxed">
                            The engine has verified <strong className="text-[#FF5E00] font-bold">{eligibleCount} qualifying schemes</strong> matching your demographic profile right now.
                        </p>
                    </div>

                    <div className="flex flex-wrap items-center gap-3 relative z-10 mt-auto">
                        <Link to="/compare" className="btn-ultra px-6 py-3 rounded-xl font-bold flex items-center gap-2 text-sm tracking-wide shadow-[0_8px_24px_rgba(255,94,0,0.3)] hover:shadow-[0_12px_32px_rgba(255,94,0,0.5)] transition-shadow duration-300">
                            <Compass size={15} /> Smart Compare
                        </Link>
                        <Link to="/results" className="btn-glass px-6 py-3 rounded-xl font-bold flex items-center gap-2 text-sm tracking-wide text-slate-700 dark:text-white">
                            Explore Schemes
                        </Link>
                    </div>
                </div>

                {/* CIVIC SCORE */}
                <div id="tour-civic-score" className="lg:col-span-4 pro-card p-7 flex flex-col justify-between relative overflow-hidden group">
                    <div className="absolute inset-0 bg-gradient-to-br from-[#FF5E00]/5 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none" />

                    <div>
                        <h3 className="text-[10px] font-black text-slate-500 dark:text-zinc-400 uppercase tracking-[0.2em] mb-1">Eligibility Score</h3>
                        <p className="text-[10px] text-slate-400 dark:text-zinc-500 font-medium">% of evaluated schemes you qualify for</p>
                    </div>
                    
                    <div className="relative w-36 h-36 flex flex-col items-center justify-center my-4 mx-auto">
                        {/* SVG Ring */}
                        <svg className="absolute inset-0 w-full h-full -rotate-90" viewBox="0 0 144 144">
                            <circle cx="72" cy="72" r="62" fill="none" stroke="currentColor" strokeWidth="8" className="text-slate-100 dark:text-zinc-800" />
                            <circle cx="72" cy="72" r="62" fill="none" stroke="url(#scoreGrad)" strokeWidth="8" strokeLinecap="round"
                                strokeDasharray={`${2 * Math.PI * 62}`}
                                strokeDashoffset={`${2 * Math.PI * 62 * (1 - (loading ? 0 : civicScore) / 100)}`}
                                className="transition-all duration-1000 ease-out drop-shadow-[0_0_8px_rgba(255,94,0,0.6)]"
                            />
                            <defs>
                                <linearGradient id="scoreGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                                    <stop offset="0%" stopColor="#FF7E20" />
                                    <stop offset="100%" stopColor="#FF3D00" />
                                </linearGradient>
                            </defs>
                        </svg>
                        <span className="text-4xl font-black text-slate-900 dark:text-white tracking-tighter">
                            {loading ? '—' : civicScore}<span className="text-lg text-[#FF5E00]">%</span>
                        </span>
                        <span className="text-[9px] uppercase font-black text-slate-400 dark:text-zinc-500 tracking-widest mt-0.5">
                            {civicScore >= 70 ? 'Excellent' : civicScore >= 40 ? 'Good' : 'Building'}
                        </span>
                    </div>

                    <div className="w-full flex justify-between items-center text-xs font-semibold text-slate-500 dark:text-zinc-400 bg-slate-50 dark:bg-[#11151C] px-4 py-3 rounded-xl border border-slate-100 dark:border-white/5">
                        <span>Profile Completion</span>
                        <span className="text-slate-900 dark:text-white font-black">{profileCompletion}%</span>
                    </div>
                </div>
            </div>

            {/* Stats Row */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                {stats.map((stat, i) => (
                    <Link key={i} to={stat.link}
                        className="flex flex-col p-4 bg-white dark:bg-[#0B0E14] border border-slate-100 dark:border-white/5 rounded-2xl hover:border-slate-200 dark:hover:border-white/10 transition-all duration-300 group shadow-sm hover:shadow-md relative overflow-hidden"
                        style={{ animationDelay: `${i * 80}ms` }}
                    >
                        <div className={`absolute top-0 left-0 right-0 h-0.5 ${stat.bg} opacity-0 group-hover:opacity-100 transition-opacity`} />
                        <div className="flex justify-between items-start mb-3">
                            <div className={`w-8 h-8 rounded-lg ${stat.bg} border ${stat.border} flex items-center justify-center`}>
                                <stat.icon size={14} className={stat.color} strokeWidth={2.5} />
                            </div>
                        </div>
                        <span className="text-2xl font-black text-slate-900 dark:text-white tracking-tighter mb-1">
                            {loading ? <div className="w-5 h-5 border-2 border-slate-200 dark:border-zinc-700 border-t-slate-400 dark:border-t-zinc-400 rounded-full animate-spin" /> : stat.val}
                        </span>
                        <span className="text-[10px] font-bold text-slate-500 dark:text-zinc-500 uppercase tracking-wide leading-tight">{stat.label}</span>
                        <span className="text-[9px] text-slate-400 dark:text-zinc-600 font-medium mt-0.5 leading-tight hidden sm:block">{stat.description}</span>
                    </Link>
                ))}
            </div>

            {/* Bottom Row */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
                
                {/* AI Recommendations */}
                <div id="tour-ai-recs" className="lg:col-span-8 flex flex-col pro-card overflow-hidden">
                    <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100 dark:border-white/5">
                        <h2 className="text-[10px] font-black text-slate-500 dark:text-zinc-400 uppercase tracking-widest sm:tracking-[0.2em] flex items-center gap-2 shrink-0">
                            <Sparkles size={12} className="text-[#FF5E00]" /> AI Recommendations
                        </h2>
                        <Link to="/results" className="text-[10px] font-black text-[#FF5E00] hover:text-[#FF7E20] transition-colors flex items-center gap-1 uppercase tracking-widest whitespace-nowrap">
                            View All <MoveRight size={10} />
                        </Link>
                    </div>
                    
                    <div className="flex flex-col divide-y divide-slate-50 dark:divide-white/5 bg-white dark:bg-[#0B0E14]">
                        {loading ? (
                            <div className="p-16 flex justify-center">
                                <div className="w-7 h-7 border-2 border-slate-100 dark:border-zinc-800 border-t-[#FF5E00] rounded-full animate-spin" />
                            </div>
                        ) : topPicks.length > 0 ? (
                            topPicks.map((pick, i) => (
                                <div key={i} onClick={() => navigate(`/scheme/${pick.scheme_id}`, { state: { resultData: pick } })}
                                    className="flex items-center justify-between gap-4 p-4 hover:bg-slate-50/80 dark:hover:bg-white/[0.02] transition-all duration-200 cursor-pointer group relative"
                                    style={{ animationDelay: `${i * 60}ms` }}
                                >
                                    <div className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-0 bg-[#FF5E00] group-hover:h-8 transition-all duration-300 rounded-r shadow-[0_0_8px_rgba(255,94,0,0.5)]" />
                                    
                                    <div className="flex flex-col gap-1.5 pl-2 min-w-0">
                                        <h3 className="text-sm font-bold text-slate-800 dark:text-white group-hover:text-[#FF5E00] transition-colors duration-200 line-clamp-1">
                                            {pick.scheme_name}
                                        </h3>
                                        <div className="flex flex-wrap items-center gap-2">
                                            <span className={`px-2 py-0.5 rounded-md text-[8px] font-black uppercase tracking-widest border ${pick.status === 'Eligible' ? 'bg-emerald-50 dark:bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-100 dark:border-emerald-500/20' : 'bg-amber-50 dark:bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-100 dark:border-amber-500/20'}`}>
                                                {pick.status}
                                            </span>
                                            <span className="text-[10px] font-semibold text-slate-400 dark:text-zinc-400">
                                                {(pick.score_percentage || 0).toFixed(0)}% match confidence
                                            </span>
                                        </div>
                                    </div>
                                    
                                    <div className="w-7 h-7 rounded-lg bg-slate-100 dark:bg-[#11151C] border border-slate-200 dark:border-white/5 flex items-center justify-center text-slate-400 shrink-0 group-hover:bg-[#FF5E00] group-hover:border-[#FF5E00] group-hover:text-white transition-all duration-200">
                                        <MoveRight size={12} />
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="p-14 text-center flex flex-col items-center justify-center">
                                <div className="w-12 h-12 rounded-2xl bg-slate-50 dark:bg-[#11151C] border border-slate-100 dark:border-white/5 flex items-center justify-center mb-4">
                                    <HelpCircle size={22} className="text-slate-300 dark:text-zinc-600" />
                                </div>
                                <span className="text-sm font-bold text-slate-700 dark:text-white mb-1 tracking-tight">No recommendations yet</span>
                                <p className="text-xs text-slate-400 dark:text-zinc-500 max-w-sm mb-5 leading-relaxed">Complete your profile so the AI engine can match you to qualifying government schemes.</p>
                                <Link to="/onboarding/details" className="btn-ultra px-5 py-2 rounded-lg text-xs font-bold tracking-widest uppercase">Complete Profile</Link>
                            </div>
                        )}
                    </div>
                </div>

                {/* Demographic Matrix */}
                <div id="tour-profile-snapshot" className="lg:col-span-4 flex flex-col pro-card overflow-hidden">
                    <div className="px-5 py-4 border-b border-slate-100 dark:border-white/5">
                        <h2 className="text-[10px] font-black text-slate-500 dark:text-zinc-400 uppercase tracking-widest sm:tracking-[0.2em] flex items-center gap-2">
                            <FileCheck size={12} className="text-violet-500" /> Your Profile Snapshot
                        </h2>
                    </div>
                    {profile ? (
                        <div className="px-5 py-3 flex flex-col gap-0 text-sm bg-white dark:bg-[#0B0E14] flex-1">
                            {[
                                ['Sector', profile.ruralUrban || '—'],
                                ['Annual Income', profile.income ? `₹${parseInt(profile.income).toLocaleString('en-IN')}` : '—'],
                                ['Occupation', profile.occupation || '—'],
                                ['State', profile.state || '—'],
                            ].map(([k, v]) => (
                                <div key={k} className="flex justify-between items-center py-3 border-b border-slate-50 dark:border-white/5 last:border-0">
                                    <span className="text-[10px] font-bold text-slate-400 dark:text-zinc-500 uppercase tracking-wide">{k}</span>
                                    <span className="text-xs font-black text-slate-800 dark:text-white">{v}</span>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="p-8 text-center text-sm font-semibold text-slate-400 dark:text-zinc-500 flex-1 flex flex-col items-center justify-center">
                            Profile not loaded.
                        </div>
                    )}
                    <button onClick={() => navigate('/profile')} className="mx-4 mb-2 mt-2 px-4 py-2.5 rounded-xl bg-slate-50 dark:bg-[#11151C] border border-slate-100 dark:border-white/5 text-[10px] font-black text-slate-500 dark:text-zinc-400 hover:text-[#FF5E00] hover:border-[#FF5E00]/30 transition-all duration-200 uppercase tracking-widest">
                        Edit Profile Parameters
                    </button>
                    <button onClick={() => navigate('/yojana-card')} className="mx-4 mb-4 mt-0 px-4 py-2.5 rounded-xl bg-orange-500/10 dark:bg-orange-500/20 border border-orange-500/30 text-[10px] font-black text-orange-600 dark:text-orange-400 hover:bg-orange-500 hover:text-white transition-all duration-200 uppercase tracking-widest flex justify-center items-center gap-2">
                        <QrCode size={12} /> View Digital Yojana Card
                    </button>
                </div>
            </div>
            
        </div>
    );
};

export default DashboardPage;
