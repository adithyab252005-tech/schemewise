import { useState } from 'react';

const SlmTesting = () => {
    const [jsonPayload, setJsonPayload] = useState('{\n  "missing_conditions": ["Income requirement > 1 Lakh"],\n  "scheme_name": "Test Scheme"\n}');
    const [output, setOutput] = useState('');
    const [loading, setLoading] = useState(false);

    // In production, you would call a standalone backend test endpoint for the SLM
    const handleTest = () => {
        setLoading(true);
        // Simulating the SLM wait time
        setTimeout(() => {
            setOutput(JSON.stringify({
                summary: "Income ceiling breached.",
                detailed_explanation: "The scheme specifies a strict requirement that your income must be... ",
                improvement_suggestion: "Attempt to provide documentation... ",
                next_steps: ["Step 1", "Step 2"],
                confidence_note: "High confidence based on rules."
            }, null, 2));
            setLoading(false);
        }, 1500);
    };

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6">SLM Diagnostics Workbench</h2>

            <div className="grid grid-cols-2 gap-6">
                <div className="card">
                    <h3 className="font-bold mb-4">Input Evaluation Hook</h3>
                    <textarea
                        className="form-control"
                        rows="15"
                        style={{ fontFamily: 'monospace' }}
                        value={jsonPayload}
                        onChange={(e) => setJsonPayload(e.target.value)}
                    />
                    <button onClick={handleTest} className="btn btn-primary mt-4" disabled={loading}>
                        {loading ? 'Executing on Local SLM...' : 'Run Llama-3.2:3b'}
                    </button>
                </div>

                <div className="card">
                    <h3 className="font-bold mb-4">Structured SLM Output (JSON)</h3>
                    <div style={{ background: '#09090b', color: '#38bdf8', padding: '16px', borderRadius: 'var(--radius)', minHeight: '300px', fontFamily: 'monospace', whiteSpace: 'pre-wrap' }}>
                        {output || 'Waiting for execution...'}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SlmTesting;
