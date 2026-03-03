// React
import React from 'react';
// umi
import { Outlet } from 'umi';

const Layout: React.FC = () => {
    return (
        <div>
            <Outlet />
        </div>
    );
};

export default Layout;
