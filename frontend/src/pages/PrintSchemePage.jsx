import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Printer, Shield, CheckCircle2 } from 'lucide-react';
import { useSchemes } from '../hooks/useSchemes';
import { useProfile } from '../context/ProfileContext';

const PrintSchemePage = () => {
    const { id } = useParams();
    const { fetchSchemeDetails } = useSchemes();
    const { getActiveProfile } = useProfile();
    
    const [scheme, setScheme] = useState(null);
    const [aiGuide, setAiGuide] = useState('');
    const [loading, setLoading] = useState(true);
    const profile = getActiveProfile();

    useEffect(() => {
        const loadPrintData = async () => {
            // Fetch Scheme
            const schemeData = await fetchSchemeDetails(id);
            if (!schemeData) {
                setLoading(false);
                return;
            }
            setScheme(schemeData);

            // Fetch Custom AI Application Guide
            try {
                const basePrompt = `Please provide a highly detailed, step-by-step guide on how and where to apply for the scheme "${schemeData.scheme_name}". Include exact document requirements, portal URLs if possible, and specific application procedures. Structure it in a professional, easily actionable format. DO NOT use markdown formatting like asterisks or hashes. Use plain text with simple dashes for lists. Format exactly for physical print formatting.`;
                
                const res = await fetch('http://180.235.121.245:8060/api/v1/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ 
                        message: basePrompt,
                        history: [],
                        user_profile: profile || {}
                    })
                });
                const data = await res.json();
                setAiGuide(data.reply);
            } catch (e) {
                setAiGuide("Official AI application guide unavailable at this moment. Please check the official portal directly.");
            }
            
            setLoading(false);
            
            // Allow DOM to paint, then trigger print dialog automatically
            setTimeout(() => {
                window.print();
            }, 800);
        };
        
        loadPrintData();
    }, [id]);

    if (loading) {
        return (
            <div className="flex h-screen flex-col items-center justify-center bg-white text-slate-800">
                <Shield className="w-12 h-12 mb-4 animate-pulse text-slate-300" />
                <h2 className="text-xl font-bold tracking-tight mb-2">Generating Official Document...</h2>
                <p className="text-sm font-medium text-slate-500">Compiling scheme data and executing custom AI reasoning layout. This will prompt your printer shortly.</p>
            </div>
        );
    }

    if (!scheme) {
        return <div className="p-10 text-center font-bold text-slate-500">Record Not Found</div>;
    }

    return (
        <div className="bg-white min-h-screen text-black w-full" style={{ fontFamily: 'Arial, sans-serif' }}>
            <div className="max-w-4xl mx-auto p-8 sm:p-12 print:p-0">
                
                {/* ── Document Header ─────────────────────────────────────── */}
                <div className="border-b-4 border-slate-900 pb-8 mb-8 flex justify-between items-start">
                    <div>
                        <div className="flex items-center gap-2 mb-2">
                            <div className="w-10 h-10 bg-slate-900 text-white font-black flex items-center justify-center rounded text-xl shadow-sm">S</div>
                            <span className="text-2xl font-black tracking-tighter">SchemeWise</span>
                        </div>
                        <p className="text-xs font-bold text-slate-500 tracking-widest uppercase">Verified Official Document</p>
                    </div>
                    <div className="text-right">
                        <button onClick={() => window.print()} className="print:hidden flex items-center justify-center gap-2 float-right mb-4 px-4 py-2 border-2 border-slate-900 text-slate-900 font-bold rounded shadow-md hover:bg-slate-900 hover:text-white transition-all">
                            <Printer size={16} /> PRINT MANUALLY
                        </button>
                        <div className="clear-both"></div>
                        <p className="text-sm font-bold text-slate-600">Generated: {new Date().toLocaleDateString()}</p>
                        <p className="text-xs font-medium text-slate-500 mt-1">Ref ID: SW-{scheme.scheme_id}-PR</p>
                    </div>
                </div>

                {/* ── Context & Metadata ─────────────────────────────────────── */}
                <div className="mb-10">
                    <div className="inline-block px-3 py-1 bg-slate-100 border border-slate-300 text-slate-800 text-[10px] font-black uppercase tracking-widest mb-4">
                        {scheme.scheme_category} | {scheme.scheme_type}
                    </div>
                    <h1 className="text-4xl font-black tracking-tight leading-tight mb-4 text-slate-900">
                        {scheme.scheme_name}
                    </h1>
                    <p className="text-base font-bold text-slate-600 flex items-center gap-2">
                        {scheme.ministry || "Official Government Scheme"}
                    </p>
                </div>

                {/* ── Demographics Matrix ─────────────────────────────────────── */}
                <h3 className="text-lg font-black uppercase tracking-widest border-b-2 border-slate-200 pb-2 mb-4">Eligibility Parameters</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10 text-sm">
                    <div className="bg-slate-50 border border-slate-200 p-4 rounded-lg">
                        <span className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Age</span>
                        <span className="font-bold underline decoration-slate-300">{scheme.target_age_min ? `${scheme.target_age_min}+` : 'Universal'}</span>
                    </div>
                    <div className="bg-slate-50 border border-slate-200 p-4 rounded-lg">
                        <span className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Income Limit</span>
                        <span className="font-bold underline decoration-slate-300">{scheme.income_max ? `₹${scheme.income_max}` : 'No Limit'}</span>
                    </div>
                    <div className="bg-slate-50 border border-slate-200 p-4 rounded-lg">
                        <span className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Sector</span>
                        <span className="font-bold underline decoration-slate-300">{scheme.rural_urban || 'Both'}</span>
                    </div>
                    <div className="bg-slate-50 border border-slate-200 p-4 rounded-lg">
                        <span className="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Category</span>
                        <span className="font-bold underline decoration-slate-300">{(scheme.eligible_categories && scheme.eligible_categories.join(', ')) || 'Any'}</span>
                    </div>
                </div>

                {/* ── Core Guidelines ─────────────────────────────────────── */}
                <h3 className="text-lg font-black uppercase tracking-widest border-b-2 border-slate-200 pb-2 mb-4">Core Guidelines</h3>
                <div className="bg-white border-l-4 border-slate-900 pl-6 py-2 mb-10">
                    <p className="text-sm font-medium leading-relaxed text-slate-700">
                        {scheme.description || "Official government program targeted at improving socio-economic outcomes. Direct benefit transfers (DBT) and validation occur directly through public portals."}
                    </p>
                </div>

                {/* ── Official Action Procedure (AI Gen) ─────────────────────────────────────── */}
                <div className="bg-slate-50 border border-slate-200 rounded-2xl p-8 mb-10 print:break-inside-avoid">
                    <h3 className="text-lg font-black uppercase tracking-widest border-b border-slate-300 pb-3 mb-6 flex items-center gap-2">
                        <Shield size={20} className="text-slate-800" /> Authorized Application Method
                    </h3>
                    <div className="text-sm font-medium leading-loose text-slate-800 whitespace-pre-wrap">
                        {aiGuide}
                    </div>
                </div>

                {/* ── Verification Action Box ─────────────────────────────────────── */}
                <div className="mt-8 border-t-2 border-slate-200 pt-8 flex items-center justify-between print:break-inside-avoid">
                    <div className="flex items-center gap-4">
                        <div className="w-16 h-16 border-2 border-slate-900 p-1">
                            {/* Visual QR Code Placeholder representing the link */}
                            <div className="w-full h-full bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-slate-900 to-slate-800 text-white flex items-center justify-center text-xs">QR</div>
                        </div>
                        <div>
                            <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Official Portal Gateway</p>
                            <a href={scheme.source_url} className="text-sm font-black underline text-slate-900">
                                {scheme.source_url || "https://myscheme.gov.in"}
                            </a>
                        </div>
                    </div>
                    <div className="text-right flex items-center gap-2 opacity-50">
                        <CheckCircle2 size={24} />
                        <span className="text-xs font-bold uppercase tracking-widest">End of Record</span>
                    </div>
                </div>

            </div>
        </div>
    );
};

export default PrintSchemePage;
