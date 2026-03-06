import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import GovernanceAssistant from '@/components/GovernanceAssistant';
import { hasToken } from '@/utils/auth';

const Layout: React.FC = () => {
    const location = useLocation();
    const showAssistant = hasToken() && location.pathname !== '/login';

    return (
        <div>
            <Outlet />
            {showAssistant && <GovernanceAssistant />}
        </div>
    );
};

export default Layout;
