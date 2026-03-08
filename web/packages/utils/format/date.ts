export const formatDateTime = (value?: string | null) => {
    if (!value) return '-';

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;

    return date.toLocaleString();
};

export const formatMonthDay = (value?: string | null) => {
    if (!value) return '-';
    return value.slice(5);
};
