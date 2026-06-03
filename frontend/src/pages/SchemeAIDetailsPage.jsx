import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Button from '../components/ui/Button';

const SchemeAIDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [scheme, setScheme] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isFetchingAI, setIsFetchingAI] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        const loadDetails = async () => {
            try {
                const res = await axios.get(`http://180.235.121.245:8060/api/v1/schemes/${id}`);
                setScheme(res.data);
            } catch (err) {
                setError('Failed to load scheme details.');
            }
            setLoading(false);
        };
        loadDetails();
    }, [id]);

    const handleAIFetch = async () => {
        setIsFetchingAI(true);
        setError('');
        try {
            const res = await axios.post(`http://180.235.121.245:8060/api/v1/schemes/${id}/fetch-details`);
            setScheme(prev => ({ ...prev, content_hash: res.data.content_hash }));
        } catch (err) {
            setError(err.response?.data?.error || 'Failed to fetch details via AI.');
        }
        setIsFetchingAI(false);
    };

    if (loading) {
        return (
            <div className="min-h-[calc(100vh-64px)] flex flex-col items-center justify-center bg-transparent">
                <div className="w-16 h-16 border-4 border-orange-600 border-t-transparent rounded-full animate-spin shadow-2xl shadow-black/5 dark:shadow-black/40"></div>
            </div>
        );
    }

    if (!scheme) {
        return <div className="text-center mt-20 text-red-500 font-bold">{error || 'Scheme not found'}</div>;
    }

    return (
        <div className="min-h-screen bg-transparent font-sans pb-24 pt-32 px-6">
            <div className="max-w-4xl mx-auto">
                <button
                    onClick={() => navigate(-1)}
                    className="flex items-center text-slate-500 dark:text-zinc-400 hover:text-slate-700 dark:text-zinc-200 transition-colors mb-8 font-medium gap-2 group"
                >
                    <span className="group-hover:-translate-x-1 transition-transform">←</span> Back to Scheme
                </button>

                <div className="mb-8">
                    <h1 className="text-3xl font-extrabold text-slate-800 dark:text-zinc-50 mb-2">Detailed AI Analysis</h1>
                    <p className="text-slate-500 dark:text-zinc-400">Deep extraction of requirements and parameters for <strong className="text-slate-600 dark:text-zinc-300">{scheme.scheme_name}</strong>.</p>
                </div>

                <div className="pro-card rounded-2xl shadow-2xl shadow-black/5 dark:shadow-black/40 transition-all p-8">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-xl font-bold flex items-center gap-2 text-orange-400">
                            ✨ AI Extracted Description
                        </h2>
                        <Button 
                            onClick={handleAIFetch} 
                            disabled={isFetchingAI}
                            className="bg-orange-600 hover:bg-orange-700 text-slate-900 dark:text-white border-none font-bold"
                        >
                            {isFetchingAI ? 'Fetching...' : 'Re-Fetch with AI'}
                        </Button>
                    </div>

                    {error && (
                        <div className="mb-6 p-4 rounded-lg bg-red-50 text-red-600 border border-red-200">
                            {error}
                        </div>
                    )}

                    <div className="bg-transparent rounded-xl p-6 whitespace-pre-wrap text-slate-600 dark:text-zinc-300 font-medium leading-relaxed min-h-[300px] border border-slate-200 dark:border-white/5 shadow-inner">
                        {scheme.content_hash ? (
                            scheme.content_hash
                        ) : (
                            <div className="h-full flex flex-col items-center justify-center text-slate-500 dark:text-zinc-400 opacity-70">
                                <span className="text-4xl mb-3 drop-shadow-2xl shadow-black/5 dark:shadow-black/40">🤖</span>
                                <p>No detailed description available yet.</p>
                                <p>Click the button above to fetch the latest details straight from the official portal using AI.</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SchemeAIDetailsPage;
