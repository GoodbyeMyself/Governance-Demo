import { request } from '@umijs/max';

const AUTH_CENTER_API_PREFIX = '/api/auth-center';

function mapCurrentUser(
  user?: API.CurrentUser,
): API.CurrentUser | undefined {
  if (!user) {
    return undefined;
  }

  return {
    ...user,
    name: user.nickname || user.username,
    userid: user.id ? String(user.id) : undefined,
    access: user.role === 'ADMIN' ? 'admin' : 'user',
  };
}

export async function currentUser(options?: { [key: string]: any }) {
  const response = await request<API.BackendApiResponse<API.CurrentUser>>(
    `${AUTH_CENTER_API_PREFIX}/me`,
    {
      method: 'GET',
      credentials: 'include',
      ...(options || {}),
    },
  );

  return {
    ...response,
    data: mapCurrentUser(response.data),
  };
}

export async function outLogin(options?: { [key: string]: any }) {
  return request<API.BackendApiResponse<null>>(`${AUTH_CENTER_API_PREFIX}/logout`, {
    method: 'POST',
    credentials: 'include',
    ...(options || {}),
  });
}

export async function login(
  body: API.LoginParams,
  options?: { [key: string]: any },
) {
  const response = await request<
    API.BackendApiResponse<{
      expiresIn?: number;
      user?: API.CurrentUser;
    }>
  >(`${AUTH_CENTER_API_PREFIX}/login`, {
    method: 'POST',
    credentials: 'include',
    skipErrorHandler: true,
    headers: {
      'Content-Type': 'application/json',
    },
    data: {
      username: body.username,
      password: body.password,
      captchaId: body.captchaId,
      captchaCode: body.captchaCode,
    },
    ...(options || {}),
  });

  const currentUser = mapCurrentUser(response.data?.user);

  return {
    success: response.success,
    message: response.message,
    status: response.success ? 'ok' : 'error',
    type: body.type,
    currentAuthority: currentUser?.access,
    data: {
      expiresIn: response.data?.expiresIn,
      user: currentUser,
    },
  } satisfies API.LoginResult;
}
