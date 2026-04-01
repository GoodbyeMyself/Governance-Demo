import type { LocaleMessages } from '../../../types';

const httpMessages: LocaleMessages = {
    'http.400': 'Invalid request parameters',
    'http.401': 'Your session has expired. Please sign in again',
    'http.403': 'You do not have permission to access this resource',
    'http.404': 'The requested endpoint was not found',
    'http.405': 'The request method is not allowed',
    'http.408': 'The request timed out. Please try again later',
    'http.409': 'Resource conflict. Please refresh and retry',
    'http.422': 'Validation failed. Please review your input',
    'http.500': 'The service is temporarily unavailable',
    'http.default': 'Request failed. Please try again later',
    'http.networkError': 'Network error. Please check your connection',
    'http.requestDataRequired': 'Missing data payload for {method} request',
    'http.requestUrlRequired': 'Request URL cannot be empty',
};

export default httpMessages;
