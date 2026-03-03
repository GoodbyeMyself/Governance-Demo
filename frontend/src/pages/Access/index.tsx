// 权限控制
import { Access, useAccess } from '@umijs/max';
// antd
import { Button } from 'antd';

const AccessPage: React.FC = () => {
    const access = useAccess();

    return (
        <div
            style={{
                minHeight: '400px',
                backgroundColor: '#fff',
            }}
        >
            <Access accessible={access.canSeeAdmin}>
                <Button>只有 Admin 可以看到这个按钮</Button>
            </Access>
        </div>
    );
};

export default AccessPage;
