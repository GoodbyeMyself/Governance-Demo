// React
import React from 'react';
// umi
import { Outlet } from 'umi';
import GovernanceAssistant from '@/components/GovernanceAssistant';

const Layout: React.FC = () => {
    return (
        <div>
            <Outlet />
            <GovernanceAssistant />
        </div>
    );
};

export default Layout;
