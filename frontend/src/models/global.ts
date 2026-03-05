import { useState } from 'react';
import { DEFAULT_NAME } from '@/constants';

const useUser = () => {
    const [name, setName] = useState<string>(DEFAULT_NAME);
    return {
        name,
        setName,
    };
};

export default useUser;
