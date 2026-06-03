import { Outlet, useLocation, Navigate } from 'react-router-dom';
import { useProfile } from '../context/ProfileContext';

const PublicLayout = () => {
    const location = useLocation();
    const { isAuthenticated } = useProfile();

    // Prevent authenticated users from hovering on the public marketing page
    if (isAuthenticated && location.pathname === '/') {
        return <Navigate to="/dashboard" replace />;
    }

    return (
        <Outlet />
    );
};

export default PublicLayout;
