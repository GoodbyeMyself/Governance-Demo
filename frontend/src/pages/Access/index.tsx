import { getStoredUser } from '@/utils/auth';
import { Button } from 'antd';

const AccessPage: React.FC = () => {
    const canSeeAdmin = getStoredUser()?.role === 'ADMIN';

    return (
        <div
            style={{
                minHeight: '400px',
                backgroundColor: '#fff',
                padding: 16,
            }}
        >
            {canSeeAdmin ? (
                <Button>仅 Admin 可见的按钮</Button>
            ) : (
                <span>当前账号无管理员权限</span>
            )}
        </div>
    );
};

export default AccessPage;
