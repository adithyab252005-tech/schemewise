import { useState } from 'react';
import { useProfile } from '../context/ProfileContext';
import { Info, Shield, FileText, LogOut, ChevronRight, User, Edit3, Lock, Bell, X, Layers, ShieldCheck, Power, Activity } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const SettingsPage = () => {
    const { logout, getActiveProfile } = useProfile();
    const navigate = useNavigate();
    const profile = getActiveProfile();
    const [activeTab, setActiveTab] = useState('profile');
    const [notificationsEnabled, setNotificationsEnabled] = useState(true);
    const [modal, setModal] = useState(null);

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    const modals = {
        about: {
            title: 'About SchemeWise',
            content: (
                <div className="space-y-4 text-sm text-slate-500 dark:text-zinc-400 leading-relaxed max-h-[60vh] overflow-y-auto pr-2 custom-scrollbar">
                    <p><strong>SchemeWise</strong> is a pioneering Digital India initiative designed to bridge the gap between citizens and government welfare programs. Our mission is to ensure that every eligible Indian can easily discover, understand, and access their rightful benefits without administrative hurdles.</p>
                    <p>Powered by an advanced AI-driven eligibility engine, SchemeWise analyzes over 4,200 central and state-level schemes in real-time. By securely matching your unique socio-economic profile against complex administrative criteria, we eliminate the guesswork and provide you with highly accurate, personalized recommendations.</p>
                    <p>Built with transparency and user privacy at its core, this platform serves as your intelligent Citizen Portal—transforming bureaucratic complexity into an intuitive, accessible experience for everyone, from student scholarships to senior citizen pensions.</p>
                    <div className="pt-4 border-t border-slate-200 dark:border-white/5">
                        <p className="font-bold text-slate-700 dark:text-zinc-200">Contact Us</p>
                        <p>Support: support@schemewise.gov.in</p>
                        <p>Partnerships: partnerships@schemewise.gov.in</p>
                    </div>
                </div>
            )
        },
        disclaimer: {
            title: 'Legal Disclaimer',
            content: (
                <div className="space-y-4 text-sm text-slate-500 dark:text-zinc-400 leading-relaxed max-h-[60vh] overflow-y-auto pr-2 custom-scrollbar">
                    <p>SchemeWise is an independent informational platform and is <strong>not an official government portal</strong>. The information provided herein is aggregated from publicly available sources, press releases, and official government announcements.</p>
                    <p>While we strive to keep the information up-to-date and accurate, SchemeWise makes no representations or warranties of any kind, express or implied, about the completeness, accuracy, reliability, or availability with respect to the schemes mentioned.</p>
                    <p><strong>Eligibility Estimates:</strong> The eligibility assessments provided by our AI engine are strictly estimates based on the profile data you input. They do not guarantee approval, entitlement, or disbursement of funds. Final eligibility, approval, and processing are solely at the discretion of the respective government authorities and nodal implementing agencies.</p>
                    <p>Users are strongly advised to verify the details from the official issuing authority's website before making any financial or administrative decisions. SchemeWise shall not be held liable for any loss or damage arising from the use of this platform.</p>
                </div>
            )
        },
        terms: {
            title: 'Terms of Service',
            content: (
                <div className="space-y-4 text-sm text-slate-500 dark:text-zinc-400 leading-relaxed max-h-[60vh] overflow-y-auto pr-2 custom-scrollbar">
                    <p>Welcome to SchemeWise. By accessing or using our platform, you agree to be bound by these Terms of Service. Please read them carefully.</p>
                    <h3 className="font-bold text-slate-700 dark:text-zinc-200 pt-2">1. Use of Service</h3>
                    <p>You agree to use this platform exclusively for personal, non-commercial purposes to discover welfare schemes. You shall not deploy automated scripts, scrapers, or bots to extract data from our service, nor attempt to interfere with the platform's security or operational integrity.</p>
                    <h3 className="font-bold text-slate-700 dark:text-zinc-200 pt-2">2. User Information & Accuracy</h3>
                    <p>You are responsible for the accuracy of the profile information you provide. Providing false or misleading information may result in inaccurate scheme matching and potential account suspension. You are also responsible for safeguarding your login credentials.</p>
                    <h3 className="font-bold text-slate-700 dark:text-zinc-200 pt-2">3. Service Modifications</h3>
                    <p>We reserve the right to modify, suspend, or discontinue any part of the service at any time without prior notice. The scheme database is updated periodically, but real-time accuracy cannot be guaranteed.</p>
                    <h3 className="font-bold text-slate-700 dark:text-zinc-200 pt-2">4. Limitation of Liability</h3>
                    <p>SchemeWise shall not be liable for any indirect, incidental, or consequential damages resulting from your use of the service or reliance on the scheme data provided herein.</p>
                </div>
            )
        },
        privacy: {
            title: 'Privacy Policy',
            content: (
                <div className="space-y-4 text-sm text-slate-500 dark:text-zinc-400 leading-relaxed max-h-[60vh] overflow-y-auto pr-2 custom-scrollbar">
                    <p>Your privacy is critically important to us. This Privacy Policy outlines how we collect, use, and protect your information.</p>
                    <h3 className="font-bold text-slate-700 dark:text-zinc-200 pt-2">1. Information Collection</h3>
                    <p>We collect personal information that you voluntarily provide during registration and profile creation, including but not limited to your name, age, gender, income, state of residence, and occupation. This data is essential for our AI to perform accurate eligibility matching.</p>
                    <h3 className="font-bold text-slate-700 dark:text-zinc-200 pt-2">2. Data Usage</h3>
                    <p>Your data is used strictly for generating personalized scheme recommendations and improving the platform's user experience. We do not sell, rent, or lease your personal socio-economic data to third-party marketing agencies.</p>
                    <h3 className="font-bold text-slate-700 dark:text-zinc-200 pt-2">3. Data Security</h3>
                    <p>We implement industry-standard security protocols, including end-to-end encryption for data in transit and secure database hashing for passwords, to protect your sensitive information from unauthorized access.</p>
                    <h3 className="font-bold text-slate-700 dark:text-zinc-200 pt-2">4. Third-Party Links</h3>
                    <p>Our platform contains links to official government websites for scheme application. Once you leave our platform, our Privacy Policy no longer applies, and we encourage you to review the privacy policies of those external sites.</p>
                </div>
            )
        }
    };

    const TABS = [
        { id: 'profile', icon: User, label: 'Profile' },
        { id: 'security', icon: ShieldCheck, label: 'Security' },
        { id: 'preferences', icon: Bell, label: 'Preferences' },
        { id: 'legal', icon: FileText, label: 'Legal & Info' },
    ];

    const firstName = profile?.name?.split(' ')[0] || 'User';

    return (
        <div className="flex flex-col gap-6 max-w-7xl mx-auto pb-12 px-4 md:px-8 pt-8 min-h-full animate-fade-in relative z-10 w-full overflow-x-hidden">
            
            {/* God-Tier Graphic Header for Settings */}
            <div className="w-full pro-card p-8 sm:p-10 flex flex-col justify-between relative overflow-hidden group mb-4">
                {/* Visual Background Effects */}
                <div className="absolute top-[-20%] right-[-5%] w-[500px] h-[500px] bg-slate-400/20 dark:bg-indigo-500/10 blur-[120px] rounded-full pointer-events-none mix-blend-screen transition-opacity group-hover:opacity-100 opacity-60"></div>
                <div className="absolute bottom-[-10%] left-[10%] w-[300px] h-[300px] bg-orange-500/10 blur-[80px] rounded-full pointer-events-none mix-blend-screen"></div>
                
                <div className="relative z-10 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-6">
                    <div>
                        <div className="flex items-center gap-3 mb-4">
                            <div className="px-3 py-1.5 rounded-md bg-orange-500/10 border border-orange-500/20 flex items-center gap-2 shadow-[0_0_15px_rgba(234,88,12,0.1)]">
                                <Activity size={12} className="text-orange-500" />
                                <span className="text-[10px] font-black text-orange-500 uppercase tracking-widest leading-none">System Configuration</span>
                            </div>
                        </div>
                        <h1 className="text-4xl font-black tracking-tighter leading-tight text-slate-900 dark:text-white drop-shadow-md">
                            Workspace <span className="text-gradient-silver">Settings</span>
                        </h1>
                        <p className="font-medium text-slate-500 dark:text-zinc-400 text-sm mt-2 max-w-sm">
                            Manage your demographic parameters, security vectors, and platform preferences.
                        </p>
                    </div>
                </div>
            </div>

            {/* Dual Pane Layout */}
            <div className="flex flex-col md:flex-row gap-8 w-full">
                
                {/* Left Navigation Pane */}
                <div className="w-full md:w-64 shrink-0 flex flex-col gap-2">
                    {TABS.map((tab) => {
                        const active = activeTab === tab.id;
                        return (
                            <button
                                key={tab.id}
                                onClick={() => setActiveTab(tab.id)}
                                className={`flex items-center gap-3 w-full px-4 py-3.5 rounded-2xl transition-all duration-300 font-bold text-sm tracking-tight ${
                                    active 
                                    ? 'bg-slate-900 dark:bg-[#11151C] text-white shadow-lg border border-transparent dark:border-white/5 shadow-orange-500/10 dark:shadow-black/50' 
                                    : 'text-slate-500 dark:text-zinc-400 hover:bg-slate-100 dark:hover:bg-[#11151C]/50 hover:text-slate-900 dark:hover:text-white'
                                }`}
                            >
                                <tab.icon size={18} className={active ? 'text-orange-500' : ''} />
                                {tab.label}
                                {active && <ChevronRight size={16} className="ml-auto opacity-50" />}
                            </button>
                        )
                    })}
                </div>

                {/* Right Content Pane */}
                <div className="flex-1 min-w-0">
                    <div className="pro-card rounded-3xl overflow-hidden shadow-2xl shadow-black/5 dark:shadow-black/40 border border-slate-200 dark:border-white/5">
                        
                        {/* Profile Tab */}
                        {activeTab === 'profile' && (
                            <div className="animate-fade-in divide-y divide-slate-100 dark:divide-white/5">
                                <div className="p-6 md:p-8 flex items-center justify-between group cursor-pointer hover:bg-slate-50 dark:hover:bg-white/[0.02] transition-colors" onClick={() => navigate('/profile')}>
                                    <div className="flex items-center gap-5">
                                        <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-emerald-500 to-emerald-400 flex items-center justify-center shadow-lg shadow-emerald-500/20">
                                            <User size={24} className="text-white drop-shadow-md" />
                                        </div>
                                        <div className="flex flex-col">
                                            <span className="text-base font-bold text-slate-900 dark:text-white mb-0.5">Demographic Matrix</span>
                                            <span className="text-xs font-medium text-slate-500 dark:text-zinc-400 max-w-xs leading-relaxed">View the raw demographic parameters used by the engine.</span>
                                        </div>
                                    </div>
                                    <ChevronRight size={20} className="text-slate-400 dark:text-zinc-500 group-hover:text-orange-500 transition-colors" />
                                </div>
                                <div className="p-6 md:p-8 flex items-center justify-between group cursor-pointer hover:bg-slate-50 dark:hover:bg-white/[0.02] transition-colors" onClick={() => navigate('/onboarding/details')}>
                                    <div className="flex items-center gap-5">
                                        <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-orange-500 to-orange-400 flex items-center justify-center shadow-lg shadow-orange-500/20">
                                            <Edit3 size={24} className="text-white drop-shadow-md" />
                                        </div>
                                        <div className="flex flex-col">
                                            <span className="text-base font-bold text-slate-900 dark:text-white mb-0.5">Update Parameters</span>
                                            <span className="text-xs font-medium text-slate-500 dark:text-zinc-400 max-w-xs leading-relaxed">Modify your income, occupation, and sector details for re-simulation.</span>
                                        </div>
                                    </div>
                                    <ChevronRight size={20} className="text-slate-400 dark:text-zinc-500 group-hover:text-orange-500 transition-colors" />
                                </div>
                            </div>
                        )}

                        {/* Security Tab */}
                        {activeTab === 'security' && (
                            <div className="animate-fade-in divide-y divide-slate-100 dark:divide-white/5">
                                <div className="p-6 md:p-8 flex items-center justify-between group cursor-pointer hover:bg-slate-50 dark:hover:bg-white/[0.02] transition-colors" onClick={() => navigate('/forgot-password')}>
                                    <div className="flex items-center gap-5">
                                        <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-amber-500 to-amber-400 flex items-center justify-center shadow-lg shadow-amber-500/20">
                                            <Lock size={24} className="text-white drop-shadow-md" />
                                        </div>
                                        <div className="flex flex-col">
                                            <span className="text-base font-bold text-slate-900 dark:text-white mb-0.5">Access Credentials</span>
                                            <span className="text-xs font-medium text-slate-500 dark:text-zinc-400 max-w-xs leading-relaxed">Reset your password and manage cryptographic access.</span>
                                        </div>
                                    </div>
                                    <ChevronRight size={20} className="text-slate-400 dark:text-zinc-500 group-hover:text-orange-500 transition-colors" />
                                </div>
                                <div className="p-6 md:p-8 flex items-center justify-between group cursor-pointer hover:bg-slate-50 dark:hover:bg-rose-900/10 transition-colors" onClick={handleLogout}>
                                    <div className="flex items-center gap-5">
                                        <div className="w-14 h-14 rounded-2xl bg-white dark:bg-[#11151C] border border-rose-200 dark:border-rose-500/20 flex items-center justify-center shadow-lg group-hover:border-rose-500 transition-colors">
                                            <Power size={24} className="text-rose-500" />
                                        </div>
                                        <div className="flex flex-col">
                                            <span className="text-base font-bold text-rose-600 dark:text-rose-500 mb-0.5">Terminate Session</span>
                                            <span className="text-xs font-medium text-slate-500 dark:text-zinc-400 max-w-xs leading-relaxed">Securely sign out of your local client on this device.</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Preferences Tab */}
                        {activeTab === 'preferences' && (
                            <div className="animate-fade-in divide-y divide-slate-100 dark:divide-white/5">
                                <div className="p-6 md:p-8 flex items-center justify-between group">
                                    <div className="flex items-center gap-5">
                                        <div className="w-14 h-14 shrink-0 rounded-2xl bg-gradient-to-tr from-indigo-500 to-indigo-400 flex items-center justify-center shadow-lg shadow-indigo-500/20">
                                            <Bell size={24} className="text-white drop-shadow-md" />
                                        </div>
                                        <div className="flex flex-col">
                                            <span className="text-base font-bold text-slate-900 dark:text-white mb-0.5">System Pings</span>
                                            <span className="text-xs font-medium text-slate-500 dark:text-zinc-400 max-w-xs leading-relaxed">Allow the intelligence core to send status notifications to your dashboard.</span>
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => setNotificationsEnabled(p => !p)}
                                        className={`shrink-0 relative inline-flex h-7 w-12 items-center rounded-full transition-colors ${notificationsEnabled ? 'bg-orange-500' : 'bg-slate-200 dark:bg-zinc-700'}`}
                                    >
                                        <span className={`inline-block h-5 w-5 rounded-full bg-white shadow-lg transition-transform ${notificationsEnabled ? 'translate-x-6' : 'translate-x-1'}`} />
                                    </button>
                                </div>
                            </div>
                        )}

                        {/* Legal & Info Tab */}
                        {activeTab === 'legal' && (
                            <div className="animate-fade-in divide-y divide-slate-100 dark:divide-white/5">
                                {[
                                    { id: 'about', icon: Info, color: 'sky', label: 'About SchemeWise Digital India' },
                                    { id: 'disclaimer', icon: Shield, color: 'slate', label: 'Legal Disclaimer' },
                                    { id: 'terms', icon: FileText, color: 'slate', label: 'Terms of Service' },
                                    { id: 'privacy', icon: Shield, color: 'slate', label: 'Privacy Policy' },
                                ].map((item) => (
                                    <div key={item.id} className="p-6 md:p-6 flex items-center justify-between group cursor-pointer hover:bg-slate-50 dark:hover:bg-white/[0.02] transition-colors" onClick={() => setModal(item.id)}>
                                        <div className="flex items-center gap-5">
                                            <div className="w-10 h-10 rounded-xl bg-white dark:bg-[#0B0E14] border border-slate-200 dark:border-white/5 flex items-center justify-center shadow-sm group-hover:border-orange-500/30 transition-colors">
                                                <item.icon size={18} className="text-slate-600 dark:text-zinc-300" />
                                            </div>
                                            <span className="text-sm font-bold text-slate-700 dark:text-zinc-200 group-hover:text-orange-500 transition-colors">{item.label}</span>
                                        </div>
                                        <ChevronRight size={18} className="text-slate-400 dark:text-zinc-500 group-hover:text-orange-500 transition-colors" />
                                    </div>
                                ))}

                                <div className="p-8 flex flex-col items-center justify-center bg-slate-50 dark:bg-[#06080A]">
                                    <div className="w-12 h-12 bg-orange-600 text-white rounded-2xl flex items-center justify-center font-bold text-lg mx-auto shadow-lg mb-4">SW</div>
                                    <p className="text-[10px] font-black text-slate-500 dark:text-zinc-500 uppercase tracking-[0.2em] mb-1">SchemeWise Intelligence Node</p>
                                    <p className="text-xs font-medium text-slate-400 dark:text-zinc-600">v1.2.4 (Build 42) — Client Initialized</p>
                                </div>
                            </div>
                        )}
                        
                    </div>
                </div>
            </div>

            {/* Modal */}
            {modal && modals[modal] && (
                <div className="fixed inset-0 z-50 bg-slate-900/60 dark:bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 animate-fade-in" onClick={() => setModal(null)}>
                    <div className="pro-card rounded-3xl shadow-2xl max-w-lg w-full p-8 scale-in" onClick={e => e.stopPropagation()}>
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-xl font-black text-slate-900 dark:text-white tracking-tight">{modals[modal].title}</h2>
                            <button onClick={() => setModal(null)} className="w-8 h-8 rounded-full bg-slate-100 dark:bg-white/5 flex items-center justify-center text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-white transition-colors">
                                <X size={16} />
                            </button>
                        </div>
                        {modals[modal].content}
                        <button onClick={() => setModal(null)} className="mt-8 w-full py-3.5 btn-ultra rounded-xl font-bold text-sm tracking-wide">Acknowledge</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SettingsPage;
