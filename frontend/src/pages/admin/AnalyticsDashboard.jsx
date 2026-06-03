import { useState, useEffect } from 'react';
import axios from 'axios';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Legend } from 'recharts';

const AnalyticsDashboard = () => {
    const [stats, setStats] = useState({
        total_users: 0,
        total_schemes: 0,
        active_schemes: 0,
        evaluations_today: 0,
        demographics: []
    });
    const [stressData, setStressData] = useState(null);
    const [runningStress, setRunningStress] = useState(false);

    const COLORS = ['#10b981', '#6366f1', '#f59e0b', '#ef4444', '#8b5cf6'];

    useEffect(() => {
        const fetchAnalytics = async () => {
            try {
                const res = await axios.get('http://180.235.121.245:8060/api/v1/admin/analytics');
                setStats(res.data);
            } catch (error) {
                console.error('Failed to fetch analytics', error);
            }
        };
        fetchAnalytics();
    }, []);

    const runStressTest = async () => {
        setRunningStress(true);
        try {
            const res = await axios.get('http://180.235.121.245:8060/api/v1/admin/stress-test');
            setStressData(res.data);
        } catch (error) {
            console.error('Failed to run stress test', error);
        } finally {
            setRunningStress(false);
        }
    };

    return (
        <div className="max-w-6xl mx-auto">
            <div className="mb-8">
                <h1 className="text-3xl font-display font-bold text-slate-900 dark:text-white tracking-tight mb-2">Platform Analytics</h1>
                <p className="text-slate-500 dark:text-zinc-400">Insights into demographic usage and traffic volumes.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-24 h-24 bg-brand-500/10 rounded-full blur-[40px] pointer-events-none" />
                    <div className="text-slate-500 dark:text-zinc-400 text-sm font-medium mb-1">Total Users</div>
                    <div className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">{stats.total_users}</div>
                </div>
                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-24 h-24 bg-emerald-500/10 rounded-full blur-[40px] pointer-events-none" />
                    <div className="text-slate-500 dark:text-zinc-400 text-sm font-medium mb-1">Total Schemes</div>
                    <div className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">{stats.total_schemes}</div>
                </div>
                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-24 h-24 bg-amber-500/10 rounded-full blur-[40px] pointer-events-none" />
                    <div className="text-slate-500 dark:text-zinc-400 text-sm font-medium mb-1">Active Schemes</div>
                    <div className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">{stats.active_schemes}</div>
                </div>
                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-24 h-24 bg-rose-500/10 rounded-full blur-[40px] pointer-events-none" />
                    <div className="text-slate-500 dark:text-zinc-400 text-sm font-medium mb-1">Evaluations Today</div>
                    <div className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">{stats.evaluations_today}</div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6">
                    <h3 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight mb-6">User Demographics (By State)</h3>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={stats.demographics}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={80}
                                    paddingAngle={5}
                                    dataKey="count"
                                    nameKey="state"
                                >
                                    {stats.demographics.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    contentStyle={{ backgroundColor: '#09090b', borderColor: '#18181b', borderRadius: '8px', color: '#fff' }}
                                    itemStyle={{ color: '#e2e8f0' }}
                                />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                    <div className="flex flex-wrap gap-4 mt-6 justify-center">
                        {stats.demographics.map((entry, index) => (
                            <div key={entry.state} className="flex items-center gap-2 text-sm text-slate-500 dark:text-zinc-400">
                                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }}></div>
                                {entry.state || 'Unknown'} ({entry.count})
                            </div>
                        ))}
                    </div>
                </div>

                <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl p-6">
                    <div className="flex justify-between items-start mb-6">
                        <div>
                            <h3 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight mb-1">Policy Stress Test</h3>
                            <p className="text-xs text-slate-500 dark:text-zinc-400">Simulates user cohort vs active schemes to find systemic exclusions.</p>
                        </div>
                        <button 
                            onClick={runStressTest} 
                            disabled={runningStress}
                            className="bg-brand-600 hover:bg-brand-500 text-slate-900 dark:text-white text-xs font-bold px-4 py-2 rounded-lg transition-colors disabled:opacity-50"
                        >
                            {runningStress ? 'Running Matrix...' : 'Run Stress Test'}
                        </button>
                    </div>

                    {stressData ? (
                        <div className="flex flex-col gap-5 h-64 overflow-y-auto pr-2 custom-scrollbar">
                            <div className="flex gap-4 mb-2">
                                <div className="bg-white dark:bg-zinc-900 px-3 py-1.5 rounded-lg text-xs font-bold text-emerald-400">Tested: {stressData.users_tested} Users</div>
                                <div className="bg-white dark:bg-zinc-900 px-3 py-1.5 rounded-lg text-xs font-bold text-amber-400">Against: {stressData.schemes_tested} Schemes</div>
                            </div>
                            
                            {stressData.results.map((scheme, idx) => (
                                <div key={idx} className="bg-white dark:bg-white dark:bg-zinc-900/50 rounded-lg p-4 border border-slate-200 dark:border-slate-200 dark:border-zinc-800/50">
                                    <h4 className="text-brand-300 font-bold text-sm mb-3 border-b border-slate-200 dark:border-white/5 pb-2">{scheme.scheme_name}</h4>
                                    <div className="flex flex-col gap-2">
                                        {scheme.reasons.map((reason, ridx) => (
                                            <div key={ridx} className="flex items-center justify-between text-xs">
                                                <span className="text-slate-500 dark:text-zinc-400">{reason.name}</span>
                                                <div className="flex border-zinc-600 items-center gap-2">
                                                    <div className="w-24 bg-zinc-800 h-1.5 rounded-full overflow-hidden">
                                                        <div className="bg-rose-500 h-full" style={{ width: `${Math.min((reason.value / stressData.users_tested) * 100, 100)}%` }}></div>
                                                    </div>
                                                    <span className="text-rose-400 font-bold w-6 text-right">{reason.value}</span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="h-64 flex flex-col items-center justify-center text-slate-500 dark:text-zinc-400 border-2 border-dashed border-slate-300 dark:border-zinc-900 rounded-xl">
                            <span className="text-3xl mb-2">🧪</span>
                            <span className="text-sm font-medium">Run a stress test to see systemic bottlenecks</span>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AnalyticsDashboard;
