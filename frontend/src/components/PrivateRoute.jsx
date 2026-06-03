import { Navigate, Outlet } from 'react-router-dom';
import { useProfile } from '../context/ProfileContext';

const PrivateRoute = () => {
    const { isAuthenticated } = useProfile();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
};

export default PrivateRoute;
