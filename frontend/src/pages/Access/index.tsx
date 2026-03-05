import { Access, useAccess } from '@umijs/max';
import { Button } from 'antd';

const AccessPage: React.FC = () => {
    const access = useAccess();

    return (
        <div
            style={{
                minHeight: '400px',
                backgroundColor: '#fff',
                padding: 16,
            }}
        >
            <Access accessible={access.canSeeAdmin}>
                <Button>仅 Admin 可见的按钮</Button>
            </Access>
        </div>
    );
};

export default AccessPage;
