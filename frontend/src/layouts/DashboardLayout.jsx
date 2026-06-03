import React, { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopBar from './TopBar';
import BottomNavBar from '../components/ui/BottomNavBar';

const DashboardLayout = () => {
    const [isKeyboardOpen, setIsKeyboardOpen] = useState(false);

    useEffect(() => {
        if (!window.visualViewport) return;
        const initialHeight = window.visualViewport.height;
        const handleResize = () => {
            // If viewport shrinks abruptly by >= 150px, keyboard is taking up screen space
            if (window.visualViewport.height < initialHeight - 150) {
                setIsKeyboardOpen(true);
            } else {
                setIsKeyboardOpen(false);
            }
        };

        window.visualViewport.addEventListener('resize', handleResize);
        return () => window.visualViewport.removeEventListener('resize', handleResize);
    }, []);

    return (
        <div className="flex flex-col h-screen print:h-auto print:overflow-visible w-full bg-transparent overflow-hidden text-slate-900 dark:text-white">
            <div className="flex flex-1 w-full h-full print:h-auto print:overflow-visible overflow-hidden relative">
                {/* Native Left Nav (Hidden on Mobile) */}
                <div className="print:hidden h-full flex shrink-0">
                    <Sidebar />
                </div>

                {/* Right Main Content Block */}
                <div className="flex-1 flex flex-col h-full print:h-auto print:overflow-visible bg-transparent relative overflow-hidden">
                    <div className="print:hidden w-full shrink-0">
                        <TopBar />
                    </div>
                    <main className="flex-1 overflow-y-auto print:overflow-visible print:h-auto w-full h-full relative p-4 sm:p-6 md:p-8 lg:p-10 pb-[env(safe-area-inset-bottom)] md:pb-10 section-scroll">
                        <Outlet />
                    </main>
                </div>
            </div>
            {/* Absolute fixed navigation locked inside the App Frame boundaries */}
            {!isKeyboardOpen && (
                <div className="print:hidden w-full shrink-0 z-50 md:hidden bg-white/95 dark:bg-[#06080A]/95 backdrop-blur-xl border-t border-slate-200 dark:border-white/10 shadow-[0_-10px_40px_rgba(0,0,0,0.05)]">
                    <BottomNavBar />
                </div>
            )}
        </div>
    );
};

export default DashboardLayout;
