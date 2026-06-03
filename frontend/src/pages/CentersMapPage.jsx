import { useState, useMemo, useCallback } from 'react';
import { useProfile } from '../context/ProfileContext';
import {
    MapPin, Navigation, ExternalLink, AlertTriangle,
    ChevronRight, Copy, CheckCheck, RefreshCw, Info,
    Building2, Landmark, FileText, Phone, Globe
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

// ─── Center-type definitions ───────────────────────────────────────────────
const CENTER_TYPES = [
    {
        id: 'eseva',
        label: 'e-Seva / CSC',
        icon: Building2,
        color: 'orange',
        queries: [
            'e-Seva center',
            'CSC Common Service Centre',
            'Maha e-Seva Kendra',
            'digital seva center',
        ],
        tip: 'Apply for Aadhaar, PAN, pension, PMAY and 300+ govt services here.',
        bgLight: 'bg-orange-50',
        bgDark: 'dark:bg-orange-500/10',
        border: 'border-orange-200 dark:border-orange-500/20',
        text: 'text-orange-600 dark:text-orange-400',
    },
    {
        id: 'panchayat',
        label: 'Gram Panchayat',
        icon: Landmark,
        color: 'emerald',
        queries: [
            'Gram Panchayat office',
            'Panchayat Raj office',
            'Block Development Office',
        ],
        tip: 'Physical panchayat office for rural scheme applications and certificates.',
        bgLight: 'bg-emerald-50',
        bgDark: 'dark:bg-emerald-500/10',
        border: 'border-emerald-200 dark:border-emerald-500/20',
        text: 'text-emerald-600 dark:text-emerald-400',
    },
    {
        id: 'collector',
        label: 'Collector / Tahsildar',
        icon: FileText,
        color: 'blue',
        queries: [
            'District Collector office',
            'Tahsildar office revenue',
            'taluka office revenue',
        ],
        tip: 'Revenue certificates, caste certificates, income certificates issued here.',
        bgLight: 'bg-blue-50',
        bgDark: 'dark:bg-blue-500/10',
        border: 'border-blue-200 dark:border-blue-500/20',
        text: 'text-blue-600 dark:text-blue-400',
    },
    {
        id: 'ration',
        label: 'Ration / PDS',
        icon: Building2,
        color: 'purple',
        queries: [
            'ration shop PDS',
            'fair price shop',
            'government ration center',
        ],
        tip: 'Public Distribution System shop for BPL ration card and food entitlements.',
        bgLight: 'bg-purple-50',
        bgDark: 'dark:bg-purple-500/10',
        border: 'border-purple-200 dark:border-purple-500/20',
        text: 'text-purple-600 dark:text-purple-400',
    },
];

// ─── Build a ranked, specific query from the user's location hierarchy ─────
function buildMapQuery(centerType, profile) {
    const baseQuery = centerType.queries[0];

    // Build location string from most-specific to least-specific
    const parts = [
        profile?.area,
        profile?.city,
        profile?.district,
        profile?.state,
    ].filter(Boolean);

    if (parts.length === 0) {
        // No location at all — fallback to India-wide
        return `${baseQuery} near me`;
    }

    // Use all parts for maximum specificity
    const locationStr = parts.join(', ');
    return `${baseQuery} near ${locationStr}`;
}

// ─── Build Google Maps search embed URL ────────────────────────────────────
function buildMapUrl(query, zoom = 14) {
    const encoded = encodeURIComponent(query);
    // Using Google Maps search embed — works without an API key
    return `https://maps.google.com/maps?q=${encoded}&z=${zoom}&output=embed&hl=en`;
}

// ─── Build "Open in Google Maps" link ──────────────────────────────────────
function buildOpenUrl(query) {
    return `https://www.google.com/maps/search/${encodeURIComponent(query)}`;
}

// ─── Copy-to-clipboard hook ────────────────────────────────────────────────
function useCopy() {
    const [copied, setCopied] = useState(false);
    const copy = useCallback((text) => {
        navigator.clipboard.writeText(text).then(() => {
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        });
    }, []);
    return { copied, copy };
}

// ─── Main Component ─────────────────────────────────────────────────────────
const CentersMapPage = () => {
    const { getActiveProfile } = useProfile();
    const navigate = useNavigate();
    const profile = getActiveProfile();

    const [selectedType, setSelectedType] = useState(CENTER_TYPES[0]);
    const [mapKey, setMapKey] = useState(0); // force iframe reload
    const { copied, copy } = useCopy();

    // Build the location display string
    const locationLabel = useMemo(() => {
        const parts = [profile?.area, profile?.city, profile?.district, profile?.state].filter(Boolean);
        return parts.length ? parts.join(', ') : null;
    }, [profile]);

    const hasLocation = Boolean(profile?.city || profile?.district || profile?.state);

    const currentQuery = useMemo(
        () => buildMapQuery(selectedType, profile),
        [selectedType, profile]
    );

    const mapSrc = useMemo(
        () => buildMapUrl(currentQuery),
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [currentQuery, mapKey]
    );

    const openUrl = buildOpenUrl(currentQuery);

    // ── No location set ────────────────────────────────────────────────────
    if (!hasLocation) {
        return (
            <div className="p-6 md:p-10 animate-fade-in flex flex-col items-center justify-center min-h-[60vh]">
                <div className="pro-card rounded-3xl p-10 border border-amber-200 dark:border-amber-500/20 max-w-lg w-full text-center shadow-2xl shadow-amber-500/5">
                    <div className="w-16 h-16 rounded-2xl bg-amber-50 dark:bg-amber-500/10 flex items-center justify-center mx-auto mb-5">
                        <AlertTriangle size={28} className="text-amber-500" />
                    </div>
                    <h2 className="text-xl font-black text-slate-900 dark:text-white mb-2">Location Not Set</h2>
                    <p className="text-slate-500 dark:text-zinc-400 text-sm font-medium mb-6 leading-relaxed">
                        To show nearby e-Seva centres and Panchayat offices, we need your
                        <strong className="text-slate-700 dark:text-zinc-200"> district, city/town</strong> and optionally your <strong className="text-slate-700 dark:text-zinc-200">area</strong>.
                        These are only used locally — never shared.
                    </p>
                    <button
                        onClick={() => navigate('/profile')}
                        className="px-8 py-3 bg-orange-600 hover:bg-orange-700 text-white font-bold rounded-xl text-sm transition-colors shadow-lg shadow-orange-500/20"
                    >
                        Update Profile Location →
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="p-4 md:p-6 lg:p-8 animate-fade-in space-y-6">

            {/* ── Header ─────────────────────────────────────────── */}
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                <div>
                    <h1 className="text-2xl md:text-3xl font-black text-slate-900 dark:text-white tracking-tight">
                        Assistance Centres
                    </h1>
                    <p className="text-slate-500 dark:text-zinc-400 mt-1 font-medium text-sm flex items-center gap-1.5">
                        <MapPin size={14} className="text-orange-500 shrink-0" />
                        Showing results near{' '}
                        <span className="font-bold text-slate-700 dark:text-zinc-200">
                            {locationLabel}
                        </span>
                    </p>
                </div>

                {/* Location chip + copy */}
                <div className="flex items-center gap-2 shrink-0 flex-wrap">
                    <div className="flex items-center gap-2 px-4 py-2 pro-card rounded-xl border border-slate-200 dark:border-white/5 text-sm font-bold text-slate-700 dark:text-zinc-200">
                        <Navigation size={14} className="text-orange-500" />
                        <span className="max-w-[200px] truncate">{locationLabel}</span>
                    </div>
                    <button
                        onClick={() => copy(locationLabel)}
                        title="Copy location"
                        className="p-2.5 pro-card rounded-xl border border-slate-200 dark:border-white/5 text-slate-500 hover:text-orange-500 transition-colors"
                    >
                        {copied ? <CheckCheck size={16} className="text-emerald-500" /> : <Copy size={16} />}
                    </button>
                    <button
                        onClick={() => setMapKey(k => k + 1)}
                        title="Reload map"
                        className="p-2.5 pro-card rounded-xl border border-slate-200 dark:border-white/5 text-slate-500 hover:text-orange-500 transition-colors"
                    >
                        <RefreshCw size={16} />
                    </button>
                </div>
            </div>

            {/* ── Center-type tab selector ───────────────────────── */}
            <div className="flex gap-2 overflow-x-auto pb-1 -mb-1 custom-scrollbar">
                {CENTER_TYPES.map(ct => {
                    const Icon = ct.icon;
                    const active = selectedType.id === ct.id;
                    return (
                        <button
                            key={ct.id}
                            onClick={() => setSelectedType(ct)}
                            className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-bold whitespace-nowrap transition-all border shrink-0 ${
                                active
                                    ? `${ct.bgLight} ${ct.bgDark} ${ct.border} ${ct.text} shadow-sm`
                                    : 'pro-card border-slate-200 dark:border-white/5 text-slate-500 dark:text-zinc-400 hover:border-slate-300 dark:hover:border-white/10'
                            }`}
                        >
                            <Icon size={15} />
                            {ct.label}
                        </button>
                    );
                })}
            </div>

            {/* ── Tip banner ─────────────────────────────────────── */}
            <div className={`flex items-start gap-3 px-4 py-3 rounded-xl border text-sm font-medium ${selectedType.bgLight} ${selectedType.bgDark} ${selectedType.border} ${selectedType.text}`}>
                <Info size={16} className="shrink-0 mt-0.5" />
                <span>{selectedType.tip}</span>
            </div>

            {/* ── Main content: map + sidebar ────────────────────── */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

                {/* ── Left: Info sidebar ───────────────────────── */}
                <div className="lg:col-span-1 flex flex-col gap-4">

                    {/* Search query card */}
                    <div className="pro-card rounded-2xl border border-slate-200 dark:border-white/5 p-5 space-y-4">
                        <h3 className="text-xs font-black uppercase tracking-widest text-slate-500 dark:text-zinc-400">
                            Searching For
                        </h3>
                        <p className="text-sm font-bold text-slate-800 dark:text-zinc-100 leading-relaxed break-words">
                            "{currentQuery}"
                        </p>
                        <div className="text-[11px] text-slate-400 dark:text-zinc-500 font-medium">
                            Results are pulled live from Google Maps for your exact location.
                        </div>
                    </div>

                    {/* Quick-action cards */}
                    <div className="pro-card rounded-2xl border border-slate-200 dark:border-white/5 p-5 space-y-3">
                        <h3 className="text-xs font-black uppercase tracking-widest text-slate-500 dark:text-zinc-400 mb-1">
                            Quick Actions
                        </h3>

                        <a
                            href={openUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex items-center justify-between w-full px-4 py-3 rounded-xl bg-orange-600 hover:bg-orange-700 text-white font-bold text-sm transition-colors group shadow-lg shadow-orange-500/20"
                        >
                            <span className="flex items-center gap-2">
                                <Globe size={15} /> Open in Google Maps
                            </span>
                            <ExternalLink size={14} className="group-hover:translate-x-0.5 transition-transform" />
                        </a>

                        {/* Search other center types */}
                        {CENTER_TYPES.filter(ct => ct.id !== selectedType.id).map(ct => {
                            const Icon = ct.icon;
                            const q = buildMapQuery(ct, profile);
                            return (
                                <a
                                    key={ct.id}
                                    href={buildOpenUrl(q)}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="flex items-center justify-between w-full px-4 py-2.5 rounded-xl pro-card border border-slate-200 dark:border-white/5 text-sm font-bold text-slate-600 dark:text-zinc-300 hover:text-orange-500 transition-colors group"
                                >
                                    <span className="flex items-center gap-2">
                                        <Icon size={14} /> {ct.label} Maps →
                                    </span>
                                    <ChevronRight size={14} className="group-hover:translate-x-0.5 transition-transform text-slate-400" />
                                </a>
                            );
                        })}
                    </div>


                </div>

                {/* ── Right: Map iframe ─────────────────────────── */}
                <div className="lg:col-span-2 relative rounded-2xl overflow-hidden border border-slate-200 dark:border-white/5 shadow-xl shadow-black/5 min-h-[480px] bg-slate-100 dark:bg-zinc-900">

                    <iframe
                        key={mapKey}
                        title={`${selectedType.label} Map`}
                        width="100%"
                        height="100%"
                        style={{ border: 0, minHeight: '480px', display: 'block' }}
                        src={mapSrc}
                        allowFullScreen
                        loading="lazy"
                        referrerPolicy="no-referrer-when-downgrade"
                    />

                    {/* Floating location pill */}
                    <div className="absolute bottom-5 left-1/2 -translate-x-1/2 px-5 py-2.5 bg-white/95 dark:bg-[#0B0E14]/95 backdrop-blur-xl border border-slate-200 dark:border-white/10 rounded-2xl shadow-2xl flex items-center gap-2.5 pointer-events-none whitespace-nowrap max-w-[90%]">
                        <span className="w-2 h-2 bg-orange-500 rounded-full animate-pulse shadow-[0_0_8px_rgba(249,115,22,0.8)]" />
                        <span className="text-xs font-bold text-slate-800 dark:text-zinc-100 truncate">
                            {selectedType.label} near {locationLabel}
                        </span>
                    </div>

                    {/* Top-right: open link */}
                    <a
                        href={openUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="absolute top-3 right-3 flex items-center gap-1.5 px-3 py-1.5 bg-white/90 dark:bg-zinc-900/90 backdrop-blur border border-slate-200 dark:border-white/10 rounded-lg text-xs font-bold text-slate-700 dark:text-zinc-200 hover:text-orange-600 transition-colors shadow"
                    >
                        <ExternalLink size={12} /> Full Map
                    </a>
                </div>
            </div>

            {/* ── National Helplines (full-width) ───────────────── */}
            <div className="pro-card rounded-2xl border border-slate-200 dark:border-white/5 p-5">
                <h3 className="text-xs font-black uppercase tracking-widest text-slate-500 dark:text-zinc-400 mb-4 flex items-center gap-2">
                    <Phone size={13} /> National Helplines
                </h3>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                    {[
                        { name: 'CSC / e-Seva',        number: '1800-121-3468' },
                        { name: 'PM Kisan',            number: '155261'        },
                        { name: 'Grievance (CPGRAMS)', number: '1800-11-8111'  },
                        { name: 'Ayushman Bharat',     number: '14555'         },
                    ].map(h => (
                        <a
                            key={h.name}
                            href={`tel:${h.number}`}
                            className="flex flex-col gap-1 px-4 py-3 rounded-xl border border-slate-100 dark:border-white/5 bg-slate-50 dark:bg-white/[0.02] hover:border-orange-200 dark:hover:border-orange-500/20 hover:bg-orange-50 dark:hover:bg-orange-500/5 transition-all group"
                        >
                            <span className="text-[11px] font-semibold text-slate-500 dark:text-zinc-400 group-hover:text-slate-700 dark:group-hover:text-zinc-200 transition-colors">
                                {h.name}
                            </span>
                            <span className="text-base font-black text-orange-600 dark:text-orange-400 tracking-wide group-hover:underline">
                                {h.number}
                            </span>
                        </a>
                    ))}
                </div>
            </div>

            {/* ── Privacy note ──────────────────────────────────── */}
            <p className="text-center text-[11px] text-slate-400 dark:text-zinc-600 font-medium">
                Your location data never leaves your device. Map searches are performed directly from your browser via Google Maps.
            </p>
        </div>
    );
};

export default CentersMapPage;
