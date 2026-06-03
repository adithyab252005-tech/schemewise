import { Link } from 'react-router-dom';
import { ShieldCheck, Cpu, Sparkles, RefreshCw, ArrowRight, Activity } from 'lucide-react';

const features = [
    {
        icon: <Cpu size={24} strokeWidth={2} />,
        label: "Deterministic Engine",
        desc: "Advanced logic evaluates your profile against hundreds of active central and state schemes instantly.",
    },
    {
        icon: <Sparkles size={24} strokeWidth={2} />,
        label: "AI Explanations",
        desc: "Get clear, human-readable suggestions on why you qualify or how to improve your eligibility score.",
    },
    {
        icon: <RefreshCw size={24} strokeWidth={2} />,
        label: "Live Monitoring",
        desc: "Our intelligent crawlers constantly sync with government web portals to detect modifications.",
    },
];

const LandingPage = () => {
    return (
        <div className="min-h-screen flex bg-slate-50 dark:bg-[#06080A] selection:bg-orange-500/30 relative overflow-hidden">
            
            {/* Mobile Ambient Graphic Layer */}
            <img src="/landing_hero_premium.png" alt="Intelligence Grid" className="absolute inset-0 w-full h-full object-cover lg:hidden opacity-20 dark:opacity-40 mix-blend-luminosity dark:mix-blend-overlay pointer-events-none scale-105" />
            <div className="absolute inset-0 bg-gradient-to-b from-transparent via-slate-50/50 dark:via-[#06080A]/60 to-slate-50 dark:to-[#06080A] lg:hidden pointer-events-none z-0"></div>

            {/* Left Content Pane */}
            <div className="w-full lg:w-[55%] flex flex-col z-10 px-5 py-6 sm:px-8 sm:py-8 md:px-16 md:py-12 xl:px-24 xl:py-16 relative overflow-y-auto custom-scrollbar">
                
                {/* Top Nav */}
                <header className="flex flex-row items-center justify-between w-full mb-20 md:mb-32 gap-2">
                    <div className="flex items-center gap-2 sm:gap-3 group cursor-default">
                        <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-orange-600 to-orange-400 flex items-center justify-center text-white font-black text-lg sm:text-xl shadow-[0_0_20px_rgba(234,88,12,0.4)] group-hover:scale-105 transition-transform shrink-0">S</div>
                        <span className="text-lg sm:text-xl font-black text-slate-900 dark:text-white tracking-tight shrink-0 whitespace-nowrap">SchemeWise<span className="text-orange-500">.</span></span>
                    </div>
                    <div className="flex flex-row items-center gap-2 sm:gap-4 shrink-0">
                        <Link to="/login" className="text-xs sm:text-sm font-bold text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-white transition-colors whitespace-nowrap px-1">Log in</Link>
                        <Link to="/register">
                            <button className="px-3 py-2 sm:px-5 sm:py-2.5 text-xs sm:text-sm font-bold text-white tracking-wide bg-slate-900 dark:bg-[#11151C] hover:bg-orange-600 dark:hover:bg-orange-600 rounded-xl shadow-[0_8px_20px_rgba(0,0,0,0.1)] dark:shadow-[0_8px_20px_rgba(0,0,0,0.4)] border border-transparent dark:border-white/5 transition-all whitespace-nowrap">Sign up</button>
                        </Link>
                    </div>
                </header>

                <main className="flex-1 flex flex-col justify-center max-w-xl">
                    <div className="inline-flex items-center gap-2 bg-orange-500/10 border border-orange-500/20 rounded-full px-4 py-1.5 mb-8 w-max shadow-sm">
                        <Activity size={14} className="text-orange-500" />
                        <span className="text-orange-600 dark:text-orange-400 font-bold text-[10px] tracking-widest uppercase">Citizen Intelligence Platform</span>
                    </div>

                    <h1 className="text-5xl md:text-6xl lg:text-[72px] leading-[1.05] font-black text-slate-900 dark:text-white tracking-tighter mb-8 drop-shadow-sm">
                        The benefits you <br/>
                        <span className="text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-amber-500 drop-shadow-md">truly deserve.</span>
                    </h1>

                    <p className="text-lg md:text-xl text-slate-500 dark:text-zinc-400 leading-relaxed font-medium mb-12 max-w-lg">
                        AI-powered discovery for central and state schemes. Instantly map your demographic parameters directly to active government funds without the guesswork.
                    </p>

                    <div className="flex flex-col sm:flex-row gap-4 mb-20">
                        <Link to="/register">
                            <button className="w-full sm:w-auto h-14 px-8 text-white font-bold text-[15px] tracking-wide bg-gradient-to-r from-orange-600 to-orange-500 hover:scale-105 rounded-2xl shadow-[0_15px_30px_-5px_rgba(234,88,12,0.4),inset_0_1px_1px_rgba(255,255,255,0.4)] transition-all flex items-center justify-center gap-3">
                                Check Eligibility <ArrowRight size={18} />
                            </button>
                        </Link>
                        <Link to="/login">
                            <button className="w-full sm:w-auto h-14 px-8 text-slate-900 dark:text-white font-bold text-[15px] tracking-wide bg-white dark:bg-[#11151C] hover:bg-slate-50 dark:hover:bg-white/5 border border-slate-200 dark:border-white/10 rounded-2xl shadow-sm transition-all flex items-center justify-center">
                                Log in
                            </button>
                        </Link>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                        {features.map((f, i) => (
                            <div key={i} className="flex flex-col gap-3 group">
                                <div className="w-12 h-12 rounded-xl bg-white dark:bg-[#11151C] border border-slate-200 dark:border-white/10 flex items-center justify-center text-slate-400 dark:text-zinc-500 group-hover:text-orange-500 group-hover:border-orange-500/30 transition-colors shadow-sm">{f.icon}</div>
                                <div>
                                    <h3 className="text-base font-bold text-slate-900 dark:text-white tracking-tight mb-2">{f.label}</h3>
                                    <p className="text-sm text-slate-500 dark:text-zinc-400 leading-relaxed font-medium">{f.desc}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </main>
            </div>

            {/* Right Massive Graphic Pane (Only visible on large screens) */}
            <div className="hidden lg:block w-[45%] relative bg-[#06080A] overflow-hidden border-l border-white/5 shadow-[-30px_0_60px_rgba(0,0,0,0.2)] dark:shadow-[-30px_0_60px_rgba(0,0,0,0.8)] z-20">
                {/* Embedded 8K Abstract Visualization */}
                <img src="/landing_hero_premium.png" alt="SchemeWise Intelligence Grid" className="absolute inset-0 w-full h-full object-cover object-center scale-105 hover:scale-100 transition-transform duration-[3s] ease-out opacity-40 dark:opacity-80 mix-blend-luminosity dark:mix-blend-normal pointer-events-none" />
                
                {/* Sophisticated dual gradients to blend the harsh edges */}
                <div className="absolute inset-0 bg-gradient-to-t from-slate-100/10 dark:from-[#06080A] via-transparent to-transparent opacity-90 pointer-events-none"></div>
                <div className="absolute inset-0 bg-gradient-to-l from-transparent via-[#06080A]/10 dark:via-[#06080A]/20 to-slate-200 dark:to-[#06080A]/90 pointer-events-none"></div>
                
                {/* Floating graphic overlay element */}
                <div className="absolute bottom-12 right-12 pro-card backdrop-blur-md bg-white/10 dark:bg-white/5 border border-white/20 dark:border-white/10 py-4 px-6 rounded-2xl flex items-center gap-4 shadow-2xl">
                    <div className="flex h-3 w-3 relative">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-orange-400 opacity-75"></span>
                        <span className="relative inline-flex rounded-full h-3 w-3 bg-orange-500 shadow-[0_0_10px_rgba(249,115,22,1)]"></span>
                    </div>
                    <span className="text-slate-900 dark:text-white font-bold text-sm tracking-widest uppercase">System Operational</span>
                </div>
            </div>
            
        </div>
    );
};

export default LandingPage;
