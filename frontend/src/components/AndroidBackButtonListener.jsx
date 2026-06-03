import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { App as CapacitorApp } from '@capacitor/app';

const AndroidBackButtonListener = () => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    let listenerHandle = null;

    const setupListener = async () => {
      // Only register listener on native platforms
      const isNative = typeof window !== 'undefined' && window.Capacitor && window.Capacitor.isNative;
      if (!isNative) return;

      listenerHandle = await CapacitorApp.addListener('backButton', ({ canGoBack }) => {
        // Define root paths where back should exit the app
        const rootPaths = ['/dashboard', '/explore', '/bot', '/saved', '/', '/login', '/welcome'];

        if (rootPaths.includes(location.pathname)) {
           // Exit app
           CapacitorApp.exitApp();
        } else if (canGoBack) {
           navigate(-1);
        } else {
           CapacitorApp.exitApp();
        }
      });
    };

    setupListener();

    return () => {
      if (listenerHandle) {
        listenerHandle.remove();
      }
    };
  }, [navigate, location.pathname]);

  return null;
};

export default AndroidBackButtonListener;
