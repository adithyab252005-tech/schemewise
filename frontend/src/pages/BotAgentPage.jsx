import { useState, useRef, useEffect } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { Send, Shield, Zap, AlertTriangle, CheckCircle, Search } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { useLocation } from 'react-router-dom';

import { useProfile } from '../context/ProfileContext';

const BotAgentPage = () => {
    const location = useLocation();
    const { getActiveProfile } = useProfile();
    const profile = getActiveProfile();
    const [activeTab, setActiveTab] = useState('chat'); // 'chat' or 'shield'
    
    // Chat State
    const [messages, setMessages] = useState([
        { role: 'bot', text: "Hello! I am SchemeWise AI, analyzing the live directory of central and state welfare programs. How can I help you today?" }
    ]);
    const [input, setInput] = useState('');
    const [loadingChat, setLoadingChat] = useState(false);
    const messagesEndRef = useRef(null);
    const hasFiredInitial = useRef(false);

    // Shield State
    const [scamInput, setScamInput] = useState('');
    const [loadingShield, setLoadingShield] = useState(false);
    const [shieldResult, setShieldResult] = useState(null);
    const [reported, setReported] = useState(false);

    useEffect(() => { 
        if (activeTab === 'chat') {
            messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }); 
        }
    }, [messages, loadingChat, activeTab]);

    const handleSendChat = async (e, customQuery = null) => {
        if (e && e.preventDefault) e.preventDefault();
        const userMsg = customQuery || input.trim();
        if (!userMsg) return;
        
        setMessages(prev => [...prev, { role: 'user', text: userMsg }]);
        if (!customQuery) setInput('');
        setLoadingChat(true);
        try {
            // Provide an empty object if profile is null to explicitly guarantee the backend parses it.
            const res = await fetch('http://180.235.121.245:8060/api/v1/chat', { 
                method: 'POST', 
                headers: { 'Content-Type': 'application/json' }, 
                body: JSON.stringify({ message: userMsg, history: messages, user_profile: profile || {} }) 
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.error || 'Server error');
            let reply = data.reply;
            if (data.context_used?.length > 0) reply += `\n\n*(Sources validated: ${data.context_used.length})*`;
            setMessages(prev => [...prev, { role: 'bot', text: reply }]);
        } catch {
            setMessages(prev => [...prev, { role: 'bot', text: '⚠️ I am unable to connect to the inference model right now. Please try again later.' }]);
        } finally { setLoadingChat(false); }
    };

    // Auto-fire query if we navigated here from Ask AI button
    useEffect(() => {
        const initialQuery = location.state?.initialQuery;
        if (initialQuery && !hasFiredInitial.current) {
            hasFiredInitial.current = true;
            // Delay by 1 tick so state can mount before firing
            setTimeout(() => {
                handleSendChat(null, initialQuery);
                // clear history object from location to prevent double-firing on refresh
                window.history.replaceState({}, document.title);
            }, 50);
        }
    }, [location.state]);

    const handleEnterChat = (e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSendChat(e); } };

    const handleShieldScan = async () => {
        if (!scamInput.trim()) return;
        setLoadingShield(true);
        setShieldResult(null);
        setReported(false);
        try {
            const res = await fetch('http://180.235.121.245:8060/api/v1/scam-check', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ claim: scamInput })
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.error || 'Server error');
            setShieldResult(data);
        } catch (err) {
            setShieldResult({ verdict: 'ERROR', summary: 'Failed to connect to SHIELD servers. Try again.' });
        } finally {
            setLoadingShield(false);
        }
    };

    return (
        <div className="flex-1 flex flex-col h-[calc(100vh-64px)] overflow-hidden px-4 py-6 md:py-8 lg:px-8 bg-transparent">
            {/* Header & Tabs */}
            <div className="max-w-4xl mx-auto w-full mb-6 z-10 flex flex-col sm:flex-row justify-between items-center gap-4">
                <div>
                    <h1 className="text-2xl font-black text-slate-900 dark:text-white tracking-tighter flex items-center gap-2">
                        {activeTab === 'chat' ? <><Zap className="text-orange-500" /> Intelligence Center</> : <><Shield className="text-orange-500" /> Scam Firewall</>}
                    </h1>
                    <p className="text-slate-500 dark:text-zinc-400 text-sm font-medium">
                        {activeTab === 'chat' ? 'Ask anything about schemes, eligibility, and rules.' : 'Paste suspicious texts or WhatsApp forwards to detect fraud.'}
                    </p>
                </div>

                <div className="flex p-1 pro-card rounded-xl border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40">
                    <button 
                        onClick={() => setActiveTab('chat')}
                        className={`px-5 py-2 rounded-lg text-sm font-bold transition-all duration-300 ${activeTab === 'chat' ? 'bg-orange-600 text-slate-900 dark:text-white shadow-2xl shadow-black/5 dark:shadow-black/40' : 'text-slate-500 dark:text-zinc-400 hover:bg-transparent'}`}
                    >
                        AI Assistant
                    </button>
                    <button 
                        onClick={() => setActiveTab('shield')}
                        className={`px-5 py-2 rounded-lg text-sm font-bold transition-all duration-300 flex items-center gap-1 ${activeTab === 'shield' ? 'bg-orange-600 text-slate-900 dark:text-white shadow-2xl shadow-black/5 dark:shadow-black/40' : 'text-slate-500 dark:text-zinc-400 hover:bg-transparent'}`}
                    >
                        SHIELD
                    </button>
                </div>
            </div>

            {/* Content Area */}
            {activeTab === 'chat' ? (
                /* Chat Interface */
                <div className="flex-1 flex flex-col w-full max-w-4xl mx-auto pro-card rounded-3xl border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 overflow-hidden z-10">
                    <div className="flex-1 overflow-y-auto px-4 sm:px-6 py-6 custom-scrollbar">
                        <AnimatePresence initial={false}>
                            {messages.map((msg, i) => {
                                const isUser = msg.role === 'user';
                                return (
                                    <motion.div key={i} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className={`flex flex-col mb-6 w-full ${isUser ? 'items-end' : 'items-start'}`}>
                                        <div className={`max-w-[85%] sm:max-w-[75%] px-5 py-4 leading-relaxed text-[15px] shadow-2xl shadow-black/5 dark:shadow-black/40 ${isUser ? 'bg-orange-600 text-slate-900 dark:text-white rounded-2xl rounded-tr-sm' : 'bg-orange-600 text-slate-800 dark:text-zinc-100 rounded-2xl rounded-tl-sm'}`}>
                                            <ReactMarkdown remarkPlugins={[remarkGfm]}
                                                components={{
                                                    p: ({ ...props }) => <p className="mb-2 last:mb-0" {...props} />,
                                                    ul: ({ ...props }) => <ul className="list-disc pl-5 mb-3 space-y-1" {...props} />,
                                                    ol: ({ ...props }) => <ol className="list-decimal pl-5 mb-3 space-y-1" {...props} />,
                                                    li: ({ ...props }) => <li className="pl-1" {...props} />,
                                                    a: ({ ...props }) => <a className={`${isUser ? 'text-orange-200' : 'text-orange-500'} hover:underline font-bold`} {...props} />,
                                                    strong: ({ ...props }) => <strong className={`font-bold ${isUser ? 'text-slate-900 dark:text-white' : 'text-slate-900 dark:text-white'}`} {...props} />,
                                                    h3: ({ ...props }) => <h3 className="text-lg font-bold mb-2 mt-4" {...props} />,
                                                }}
                                            >{msg.text}</ReactMarkdown>
                                        </div>
                                    </motion.div>
                                );
                            })}
                            {loadingChat && (
                                <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex flex-col items-start mb-6">
                                    <div className="bg-orange-600 text-slate-500 dark:text-zinc-400 rounded-2xl rounded-tl-sm px-5 py-4 flex gap-1 shadow-2xl shadow-black/5 dark:shadow-black/40">
                                        <div className="w-2 h-2 bg-orange-600 rounded-full animate-bounce" style={{animationDelay: '0ms'}} />
                                        <div className="w-2 h-2 bg-orange-600 rounded-full animate-bounce" style={{animationDelay: '150ms'}} />
                                        <div className="w-2 h-2 bg-orange-600 rounded-full animate-bounce" style={{animationDelay: '300ms'}} />
                                    </div>
                                </motion.div>
                            )}
                        </AnimatePresence>
                        <div ref={messagesEndRef} className="h-2" />
                    </div>

                    <div className="p-3 sm:p-5 border-t border-slate-100 dark:border-white/5 bg-white dark:bg-[#0B0E14] z-20">
                        <div className="relative w-full">
                            <input 
                                type="text" 
                                value={input} 
                                onChange={e => setInput(e.target.value)} 
                                onKeyDown={handleEnterChat} 
                                disabled={loadingChat}
                                className="w-full bg-slate-50 dark:bg-zinc-900 border border-slate-200 dark:border-white/10 rounded-2xl pl-5 pr-14 py-4 text-[15px] font-medium text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 transition-all shadow-inner"
                                placeholder="Type your query..." 
                            />
                            <button 
                                onClick={handleSendChat} 
                                disabled={loadingChat || !input.trim()}
                                className="absolute right-2 top-1/2 -translate-y-1/2 w-[38px] h-[38px] bg-orange-600 hover:bg-orange-500 disabled:bg-slate-300 dark:disabled:bg-zinc-700 text-white rounded-xl flex items-center justify-center transition-all shadow-md disabled:shadow-none"
                            >
                                <Send size={16} className="-ml-0.5" />
                            </button>
                        </div>
                    </div>
                </div>
            ) : (
                /* SHIELD Interface */
                <div className="flex-1 flex flex-col w-full max-w-4xl mx-auto z-10 gap-6 overflow-y-auto custom-scrollbar pb-10">
                    <div className="pro-card rounded-3xl p-6 md:p-8 border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40">
                        <h2 className="text-xl font-bold text-slate-900 dark:text-white tracking-tight mb-4 flex items-center gap-2">
                            <Search className="text-orange-500" /> Analyze a Message
                        </h2>
                        
                        <textarea 
                            value={scamInput} 
                            onChange={e => setScamInput(e.target.value)}
                            placeholder="Paste the SMS, WhatsApp forward, or claim text here to verify if it's a real government scheme or a scam..."
                            className="w-full h-40 bg-transparent border border-slate-200 dark:border-white/5 rounded-xl p-4 text-slate-700 dark:text-zinc-200 placeholder:text-slate-500 dark:text-zinc-400 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 transition-all resize-none mb-4 custom-scrollbar shadow-inner"
                        />
                        
                        <button 
                            onClick={handleShieldScan} 
                            disabled={loadingShield || !scamInput.trim()}
                            className="w-full py-4 bg-orange-600 hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 text-slate-900 dark:text-white font-bold rounded-xl shadow-md transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                        >
                            {loadingShield ? (
                                <><div className="w-5 h-5 border-2 border-white/50 border-t-white rounded-full animate-spin" /> Analyzing against Scheme Data...</>
                            ) : (
                                <><Shield size={20} /> Run Fact-Check</>
                            )}
                        </button>
                    </div>

                    {shieldResult && (
                        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
                            <div className={`
                                pro-card rounded-3xl p-6 md:p-8 border-2 shadow-2xl shadow-black/5 dark:shadow-black/40
                                ${shieldResult.verdict === 'SCAM' ? 'border-rose-500 bg-rose-50/30' : 
                                  shieldResult.verdict === 'SUSPICIOUS' ? 'border-amber-500 bg-amber-50/30' : 
                                  'border-emerald-500 bg-emerald-50/30'}
                            `}>
                                <div className="flex items-start gap-4 mb-6">
                                    <div className={`p-4 rounded-2xl 
                                        ${shieldResult.verdict === 'SCAM' ? 'bg-rose-100 text-rose-600' : 
                                          shieldResult.verdict === 'SUSPICIOUS' ? 'bg-amber-100 text-amber-600' : 
                                          'bg-emerald-100 text-emerald-600'}`}
                                    >
                                        {shieldResult.verdict === 'SCAM' ? <AlertTriangle size={32} /> : 
                                         shieldResult.verdict === 'SUSPICIOUS' ? <Shield size={32} /> : 
                                         <CheckCircle size={32} />}
                                    </div>
                                    <div>
                                        <h3 className={`text-2xl font-black mb-1
                                            ${shieldResult.verdict === 'SCAM' ? 'text-rose-600' : 
                                              shieldResult.verdict === 'SUSPICIOUS' ? 'text-amber-600' : 
                                              'text-emerald-600'}`}
                                        >
                                            {shieldResult.verdict} DETECTED
                                        </h3>
                                        <p className="text-slate-500 dark:text-zinc-400 font-medium text-lg leading-snug">{shieldResult.summary}</p>
                                    </div>
                                </div>

                                {shieldResult.red_flags?.length > 0 && (
                                    <div className="mb-6">
                                        <h4 className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-zinc-400 mb-3">Detected Red Flags</h4>
                                        <ul className="space-y-2">
                                            {shieldResult.red_flags.map((flag, i) => (
                                                <li key={i} className="flex items-start gap-2 text-sm text-slate-600 dark:text-zinc-300 font-medium pro-card p-2.5 rounded-lg">
                                                    <span className="text-rose-500 mt-0.5">⚠️</span> {flag}
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                )}

                                <div className="bg-transparent rounded-xl p-5 border border-slate-200 dark:border-white/5">
                                    <h4 className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-zinc-400 mb-2">Recommended Action</h4>
                                    <p className="text-slate-900 dark:text-white font-bold">{shieldResult.advice}</p>
                                    
                                    {shieldResult.official_link && (
                                        <a href={shieldResult.official_link} target="_blank" rel="noopener noreferrer" className="inline-block mt-3 text-sm text-orange-500 font-bold hover:text-orange-700 transition-colors">
                                            Visit Official Website →
                                        </a>
                                    )}

                                    {(shieldResult.verdict === 'SCAM' || shieldResult.verdict === 'SUSPICIOUS') && (
                                        <button 
                                            onClick={() => setReported(true)}
                                            disabled={reported}
                                            className={`mt-4 w-full py-3 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2 ${reported ? 'bg-emerald-100 text-emerald-700 cursor-default' : 'bg-white dark:bg-zinc-900 hover:bg-slate-200 dark:hover:bg-zinc-800 text-slate-900 dark:text-white shadow-md'}`}
                                        >
                                            {reported ? <><CheckCircle size={16} /> Reported to Community Database</> : <><AlertTriangle size={16} /> Report to Community Database</>}
                                        </button>
                                    )}
                                </div>
                            </div>
                        </motion.div>
                    )}
                </div>
            )}
        </div>
    );
};

export default BotAgentPage;
