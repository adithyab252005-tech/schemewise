import { useState, useEffect } from 'react';
import axios from 'axios';

const AIControlCenter = () => {
    const [logs, setLogs] = useState("[2026-03-10] Waiting for log streams...");
    const [aiConfig, setAiConfig] = useState({ system_prompt: "You are a neutral and formal Government Scheme Explanation Assistant.", temperature: 0.3 });

    const fetchLogs = async () => {
        try {
            const res = await axios.get('http://180.235.121.245:8060/api/v1/admin/logs');
            if (res.data.logs) {
                setLogs(res.data.logs);
            }
        } catch (error) {
            console.error("Failed fetching logs");
        }
    };

    const fetchAiConfig = async () => {
        try {
            const res = await axios.get('http://180.235.121.245:8060/api/v1/admin/ai_config');
            if (res.data.system_prompt) {
                setAiConfig(res.data);
            }
        } catch (error) {
            console.error("Failed fetching config");
        }
    };

    useEffect(() => {
        fetchLogs();
        fetchAiConfig();
        const interval = setInterval(fetchLogs, 5000);
        return () => clearInterval(interval);
    }, []);

    const triggerAgent = async (agentName) => {
        try {
            await axios.post('http://180.235.121.245:8060/api/v1/admin/agents/trigger', { agent: agentName });
            alert(`${agentName} triggered successfully! Check log stream below.`);
        } catch (error) {
            alert(`Failed triggering ${agentName}`);
        }
    };

    const saveAiConfig = async () => {
        try {
            await axios.post('http://180.235.121.245:8060/api/v1/admin/ai_config', aiConfig);
            alert("AI Configuration Saved!");
        } catch (error) {
            alert("Failed saving AI config");
        }
    };

    return (
        <div className="max-w-6xl mx-auto space-y-8">
            <div className="mb-4">
                <h1 className="text-3xl font-display font-bold text-slate-900 dark:text-white tracking-tight mb-2">AI Agent Control Center</h1>
                <p className="text-slate-500 dark:text-zinc-400">Trigger background Python scrapers and tune SLM prompts.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Agent Card */}
                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6 relative overflow-hidden group">
                    <div className="absolute top-0 right-0 w-32 h-32 bg-orange-600/10 rounded-full blur-[50px] pointer-events-none" />
                    <div className="flex justify-between items-start mb-4">
                        <div>
                            <div className="flex items-center gap-2 mb-1">
                                <span className="w-2 h-2 rounded-full bg-emerald-500 shadow-[0_0_8px_#10b981]"></span>
                                <h3 className="font-bold text-lg text-slate-900 dark:text-white">DiscoveryAgent</h3>
                            </div>
                            <p className="text-sm text-slate-500 dark:text-zinc-400">Polls RSS feeds for new scheme launches.</p>
                        </div>
                        <span className="text-2xl opacity-50">📡</span>
                    </div>
                    <div className="flex gap-3 mt-6">
                        <button onClick={() => triggerAgent('discovery')} className="px-4 py-2 bg-orange-600 text-slate-900 dark:text-white rounded-lg text-sm font-semibold hover:bg-orange-600 transition-colors shadow-[0_0_15px_rgba(99,102,241,0.3)]">
                            Trigger Now
                        </button>
                    </div>
                </div>

                {/* Agent Card */}
                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6 relative overflow-hidden group">
                    <div className="absolute top-0 right-0 w-32 h-32 bg-rose-500/10 rounded-full blur-[50px] pointer-events-none" />
                    <div className="flex justify-between items-start mb-4">
                        <div>
                            <div className="flex items-center gap-2 mb-1">
                                <span className="w-2 h-2 rounded-full bg-transparent0"></span>
                                <h3 className="font-bold text-lg text-slate-900 dark:text-white">MonitoringAgent</h3>
                            </div>
                            <p className="text-sm text-slate-500 dark:text-zinc-400">Scans NLP changes in existing 4200 schemes.</p>
                        </div>
                        <span className="text-2xl opacity-50">🕵️‍♂️</span>
                    </div>
                    <div className="flex gap-3 mt-6">
                        <button onClick={() => triggerAgent('monitoring')} className="px-4 py-2 bg-white dark:bg-zinc-900 text-slate-900 dark:text-white rounded-lg text-sm font-semibold hover:bg-slate-200 dark:hover:bg-zinc-800 transition-colors">
                            Trigger Now
                        </button>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Tuning Area */}
                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6">
                    <h3 className="font-bold text-slate-900 dark:text-white tracking-tight mb-4 flex items-center gap-2">🤖 SLM Tuning</h3>
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-1">System Prompt</label>
                            <textarea
                                value={aiConfig.system_prompt}
                                onChange={(e) => setAiConfig({ ...aiConfig, system_prompt: e.target.value })}
                                className="w-full bg-[#0a0a0a] border border-slate-200 dark:border-white/5 rounded-lg p-3 text-sm text-slate-500 dark:text-zinc-400 focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none transition-all resize-none"
                                rows="4"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-500 dark:text-zinc-400 mb-1">Temperature ({aiConfig.temperature})</label>
                            <input
                                type="range" min="0" max="1" step="0.1"
                                value={aiConfig.temperature}
                                onChange={(e) => setAiConfig({ ...aiConfig, temperature: parseFloat(e.target.value) })}
                                className="w-full accent-brand-500"
                            />
                        </div>
                        <button onClick={saveAiConfig} className="px-4 py-2 bg-white dark:bg-zinc-900 text-slate-900 dark:text-white rounded-lg text-sm font-semibold hover:bg-slate-200 dark:hover:bg-zinc-800 transition-colors w-full">Save Configuration</button>
                    </div>
                </div>

                {/* Log Stream */}
                <div className="bg-black border border-slate-300 dark:border-zinc-900 rounded-xl p-4 font-mono text-sm relative">
                    <div className="absolute top-3 right-4 flex gap-2">
                        <div className="w-2.5 h-2.5 rounded-full bg-zinc-800"></div>
                        <div className="w-2.5 h-2.5 rounded-full bg-zinc-800"></div>
                        <div className="w-2.5 h-2.5 rounded-full bg-zinc-800"></div>
                    </div>
                    <h3 className="text-slate-500 dark:text-zinc-400 mb-4 font-bold tracking-widest text-xs uppercase">agent_activity.log</h3>
                    <div className="h-64 overflow-y-auto text-emerald-400/80 custom-scrollbar-dark whitespace-pre-wrap text-xs">
                        {logs}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AIControlCenter;
