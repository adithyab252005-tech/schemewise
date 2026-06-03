import { Database, Cpu, ShieldCheck, Activity, BrainCircuit, Network, Locking, Server } from 'lucide-react';

const PlatformCapabilitiesPage = () => {
    const modules = [
        {
            title: "AI Recommendation Engine",
            tag: "Core Intelligence",
            desc: "A constantly evolving neural matrix assessing 4,200+ government schemas. It cross-references household demographics with state eligibility parameters in real-time.",
            icon: BrainCircuit,
            color: "emerald"
        },
        {
            title: "Real-time Civic Sync API",
            tag: "Integration",
            desc: "Direct socket connections to state databases validating BPL, tax compliance, and identity markers autonomously without manual document upload requirements.",
            icon: Network,
            color: "indigo"
        },
        {
            title: "Administrative Control Center",
            tag: "Separate Portal",
            desc: "A secure, decoupled portal utilizing raw metric telemetry to allow verification officers and system admins to manage scheme disbursement and approve massive scale applications.",
            icon: ShieldCheck,
            color: "rose"
        },
        {
            title: "Data Analytics Pipeline",
            tag: "Telemetry",
            desc: "Processes millions of mock simulation events and user demographic trees providing deep geographical insights on scheme utilization and failure rates.",
            icon: Activity,
            color: "amber"
        }
    ];

    return (
        <div className="flex flex-col gap-6 max-w-7xl mx-auto pb-12 px-4 md:px-8 pt-8 min-h-full animate-fade-in relative z-10 w-full">
            
            {/* Header Graphic */}
            <div className="w-full bg-[#0B0E14] border border-white/5 shadow-2xl p-8 sm:p-12 rounded-3xl flex flex-col justify-between relative overflow-hidden group mb-4">
                 <img src="/dashboard_hero_abstract.png" alt="System Architecture Matrix" className="absolute inset-0 w-full h-full object-cover opacity-[0.25] mix-blend-screen group-hover:scale-110 transition-transform duration-[10s] ease-out pointer-events-none" />
                 <div className="absolute inset-0 bg-gradient-to-t from-[#0B0E14] via-[#0B0E14]/80 to-transparent pointer-events-none"></div>

                <div className="relative z-10">
                    <div className="flex items-center gap-3 mb-4">
                        <div className="px-3 py-1.5 rounded-md bg-indigo-500/10 border border-indigo-500/20 flex items-center gap-2 shadow-[0_0_15px_rgba(99,102,241,0.1)]">
                            <Server size={12} className="text-indigo-400" />
                            <span className="text-[10px] font-black text-indigo-400 uppercase tracking-widest leading-none">Architecture Unlocked</span>
                        </div>
                    </div>
                    <h1 className="text-4xl sm:text-6xl font-black tracking-tighter leading-tight text-white drop-shadow-md max-w-2xl">
                        Platform <span className="text-indigo-400">Capabilities</span>
                    </h1>
                    <p className="font-medium text-white/60 text-base mt-4 max-w-xl leading-relaxed">
                        SchemeWise operates on a massive decoupled architecture. Discover the immense backend systems powering your civic interface behind the scenes.
                    </p>
                </div>
            </div>

            {/* Architecture Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {modules.map((mod, i) => (
                    <div key={i} className="pro-card p-8 rounded-3xl border border-slate-200 dark:border-white/5 hover:border-indigo-500/30 dark:hover:border-indigo-500/30 transition-all shadow-2xl group relative overflow-hidden bg-slate-50 dark:bg-[#11151C]">
                        {/* Glow effect */}
                        <div className={`absolute top-0 right-0 w-32 h-32 bg-${mod.color}-500/10 blur-[50px] rounded-full pointer-events-none group-hover:bg-${mod.color}-500/20 transition-colors`}></div>

                        <div className="relative z-10 flex flex-col gap-4">
                            <div className={`w-14 h-14 rounded-2xl bg-white dark:bg-[#0B0E14] border border-slate-200 dark:border-white/5 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-500`}>
                                <mod.icon size={26} className={`text-${mod.color}-500 dark:text-${mod.color}-400 drop-shadow-[0_0_10px_rgba(var(--tw-colors-${mod.color}-500),0.3)]`} />
                            </div>
                            
                            <div>
                                <span className={`text-[10px] font-black uppercase tracking-[0.2em] text-${mod.color}-600 dark:text-${mod.color}-400 mb-2 block`}>{mod.tag}</span>
                                <h3 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight mb-3">
                                    {mod.title}
                                </h3>
                                <p className="text-sm font-medium text-slate-500 dark:text-zinc-400 leading-relaxed">
                                    {mod.desc}
                                </p>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Status Footer */}
            <div className="pro-card p-6 rounded-3xl border border-emerald-500/20 shadow-xl mt-4 bg-emerald-50/50 dark:bg-emerald-950/20 flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <div className="relative flex items-center justify-center w-3 h-3">
                        <span className="absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75 animate-ping"></span>
                        <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                    </div>
                    <span className="font-bold text-sm text-emerald-700 dark:text-emerald-400">All Core Backend Infrastructure Operational</span>
                </div>
                <span className="text-[10px] font-bold text-emerald-600/50 dark:text-emerald-500/50 uppercase tracking-widest hidden sm:block">Uptime: 99.99%</span>
            </div>

        </div>
    );
};

export default PlatformCapabilitiesPage;
