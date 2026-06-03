import { useState, useEffect } from 'react';
import Joyride, { STATUS } from 'react-joyride';

const OnboardingTour = () => {
    const [run, setRun] = useState(false);

    useEffect(() => {
        // Run tour only on first visit for new users
        // Add a small delay so elements mount perfectly on slower mobile devices
        const timer = setTimeout(() => {
            if (!localStorage.getItem('hasSeenDashboardTour')) {
                setRun(true);
            }
        }, 1500);
        return () => clearTimeout(timer);
    }, []);

    const handleJoyrideCallback = (data) => {
        const { status } = data;
        const finishedStatuses = [STATUS.FINISHED, STATUS.SKIPPED];

        if (finishedStatuses.includes(status)) {
            setRun(false);
            localStorage.setItem('hasSeenDashboardTour', 'true');
        }
    };

    const steps = [
        {
            target: '#tour-civic-score',
            content: (
                <div className="flex flex-col gap-2 p-1">
                    <h3 className="font-extrabold text-slate-900 text-lg leading-tight tracking-tight">Your AI Eligibility Score</h3>
                    <p className="text-sm font-medium text-slate-600 leading-relaxed">This ring constantly spins to gauge the percentage of government schemes you perfectly qualify for right now.</p>
                </div>
            ),
            disableBeacon: true,
            placement: 'bottom',
        },
        {
            target: '#tour-profile-snapshot',
            content: (
                <div className="flex flex-col gap-2 p-1">
                    <h3 className="font-extrabold text-slate-900 text-lg leading-tight tracking-tight">Keep Your Profile Linked</h3>
                    <p className="text-sm font-medium text-slate-600 leading-relaxed">The AI Engine runs on this specific data! Make sure you keep your Income, Sector, and state frequently updated.</p>
                </div>
            ),
            placement: 'top',
        },
        {
            target: '#tour-ai-recs',
            content: (
                <div className="flex flex-col gap-2 p-1">
                    <h3 className="font-extrabold text-slate-900 text-lg leading-tight tracking-tight">Guaranteed Top Matches</h3>
                    <p className="text-sm font-medium text-slate-600 leading-relaxed">These are the absolute best financial returns based on your verified data. Click any of them to view full documentation and apply right away!</p>
                </div>
            ),
            placement: 'top',
        }
    ];

    if (!run) return null;

    return (
        <Joyride
            callback={handleJoyrideCallback}
            continuous
            hideCloseButton
            run={run}
            scrollToFirstStep
            showProgress
            showSkipButton
            steps={steps}
            styles={{
                options: {
                    zIndex: 10000,
                    primaryColor: '#F97316', // brand-orange
                    backgroundColor: '#FFFFFF',
                    textColor: '#0f172a',
                    overlayColor: 'rgba(0, 0, 0, 0.75)',
                },
                buttonNext: {
                    backgroundColor: '#ea580c',
                    color: '#fff',
                    fontWeight: 800,
                    fontFamily: 'Outfit, sans-serif',
                    padding: '10px 18px',
                    borderRadius: '12px',
                    boxShadow: '0 4px 14px rgba(234, 88, 12, 0.3)',
                },
                buttonBack: {
                    color: '#64748b',
                    fontWeight: 800,
                    fontFamily: 'Outfit, sans-serif',
                    marginRight: '8px',
                },
                buttonSkip: {
                    color: '#94a3b8',
                    fontSize: '12px',
                    fontWeight: 700,
                    fontFamily: 'Outfit, sans-serif',
                },
                tooltipContainer: {
                    textAlign: 'left',
                },
                tooltip: {
                    fontFamily: 'Outfit, sans-serif',
                    padding: '20px',
                    borderRadius: '24px',
                    border: '1px solid rgba(0,0,0,0.06)',
                    boxShadow: '0 10px 30px rgba(0,0,0,0.2)',
                }
            }}
        />
    );
};

export default OnboardingTour;
