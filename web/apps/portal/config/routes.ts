export default [
  {
    path: '/user',
    layout: false,
    routes: [
      {
        path: '/user/login',
        layout: false,
        name: 'login',
        component: './user/login',
      },
      {
        path: '/user/register',
        name: 'register',
        component: './user/register',
      },
      {
        path: '/user/register-result',
        name: 'register-result',
        component: './user/register-result',
      },
      {
        path: '/user',
        redirect: '/user/login',
      },
      {
        component: '404',
        path: '/user/*',
      },
    ],
  },
  {
    path: '/',
    name: 'home',
    icon: 'home',
    component: './data-overview',
  },
  {
    path: '/account/center',
    component: './account/center',
    hideInMenu: true,
  },
  {
    path: '/account/settings',
    component: './account/settings',
    hideInMenu: true,
  },
  {
    component: '404',
    path: '/*',
  },
];
