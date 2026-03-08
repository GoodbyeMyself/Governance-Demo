export const LOGIN_PATH = '/login';
export const HOME_PATH = '/home';
export const DATA_SOURCE_PATH = '/data-source';
export const METADATA_COLLECTION_PATH = '/metadata-collection';
export const USER_MANAGEMENT_PATH = '/user-management';
export const PROFILE_PATH = '/profile';
export const ACCESS_PATH = '/access';

export const buildMetadataCollectionDetailPath = (id: number | string) =>
    `${METADATA_COLLECTION_PATH}/${id}`;

export const buildLoginRedirect = (pathname: string, search = '') => {
    const redirect = encodeURIComponent(`${pathname}${search}`);
    return `${LOGIN_PATH}?redirect=${redirect}`;
};
