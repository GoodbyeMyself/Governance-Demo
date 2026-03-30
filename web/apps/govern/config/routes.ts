/**
 * @name umi 的路由配置
 * @description 只支持 path,component,routes,redirect,wrappers,name,icon 的配置
 * @param path  path 只支持两种占位符配置，第一种是动态参数 :id 的形式，第二种是 * 通配符，通配符只能出现路由字符串的最后。
 * @param component 配置 location 和 path 匹配后用于渲染的 React 组件路径。可以是绝对路径，也可以是相对路径，如果是相对路径，会从 src/pages 开始找起。
 * @param routes 配置子路由，通常在需要为多个路径增加 layout 组件时使用。
 * @param redirect 配置路由跳转
 * @param wrappers 配置路由组件的包装组件，通过包装组件可以为当前的路由组件组合进更多的功能。 比如，可以用于路由级别的权限校验
 * @param name 配置路由的标题，默认读取国际化文件 menu.ts 中 menu.xxxx 的值，如配置 name 为 login，则读取 menu.ts 中 menu.login 的取值作为标题
 * @param icon 配置路由的图标，取值参考 https://ant.design/components/icon-cn， 注意去除风格后缀和大小写，如想要配置图标为 <StepBackwardOutlined /> 则取值应为 stepBackward 或 StepBackward，如想要配置图标为 <UserOutlined /> 则取值应为 user 或者 User
 * @doc https://umijs.org/docs/guides/routes
 */
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
        path: '/user',
        redirect: '/user/login',
      },
      {
        name: 'register-result',
        icon: 'smile',
        path: '/user/register-result',
        component: './user/register-result',
      },
      {
        name: 'register',
        icon: 'smile',
        path: '/user/register',
        component: './user/register',
      },
      {
        component: '404',
        path: '/user/*',
      },
    ],
  },
  {
    path: '/workplace',
    name: 'workplace',
    icon: 'dashboard',
    component: './workplace',
  },
  {
    path: '/chat',
    name: 'chat',
    icon: 'message',
    component: './chat',
  },
  {
    path: '/data-overview',
    name: 'data-overview',
    icon: 'areaChart',
    component: './data-overview',
  },
  {
    path: '/data-governance',
    name: 'data-governance',
    icon: 'database',
    routes: [
      {
        path: '/data-governance',
        redirect: '/data-governance/data-source',
      },
      {
        path: '/data-governance/data-source',
        name: 'data-source',
        icon: 'table',
        component: './data-governance/data-source',
      },
      {
        path: '/data-governance/metadata-collection',
        name: 'metadata-collection',
        icon: 'profile',
        component: './data-governance/metadata-collection',
      },
      {
        path: '/data-governance/device-management',
        name: 'device-management',
        icon: 'cluster',
        routes: [
          {
            path: '/data-governance/device-management',
            redirect: '/data-governance/device-management/device-link',
          },
          {
            path: '/data-governance/device-management/device-link',
            name: 'device-link',
            icon: 'link',
            component: './data-governance/device-management/device-link',
          },
          {
            path: '/data-governance/device-management/data-collection',
            name: 'data-collection',
            icon: 'api',
            component: './data-governance/device-management/data-collection',
          },
        ],
      },
    ],
  },
  {
    path: '/system-management',
    name: 'system-management',
    icon: 'setting',
    routes: [
      {
        path: '/system-management',
        redirect: '/system-management/user-management',
      },
      {
        path: '/system-management/user-management',
        name: 'user-management',
        icon: 'team',
        component: './system-management/user-management',
      },
      {
        path: '/system-management/role-management',
        name: 'role-management',
        icon: 'solution',
        component: './system-management/role-management',
      },
    ],
  },
  {
    path: '/demo',
    name: 'demo',
    icon: 'appstore',
    routes: [
      {
        path: '/demo',
        redirect: '/demo/dashboard/analysis',
      },
      {
        path: '/demo/dashboard',
        name: 'dashboard',
        icon: 'dashboard',
        routes: [
          {
            path: '/demo/dashboard',
            redirect: '/demo/dashboard/analysis',
          },
          {
            name: 'analysis',
            icon: 'smile',
            path: '/demo/dashboard/analysis',
            component: './demo/dashboard/analysis',
          },
          {
            name: 'monitor',
            icon: 'smile',
            path: '/demo/dashboard/monitor',
            component: './demo/dashboard/monitor',
          },
          {
            name: 'workplace',
            icon: 'smile',
            path: '/demo/dashboard/workplace',
            component: './demo/dashboard/workplace',
          },
        ],
      },
      {
        path: '/demo/form',
        icon: 'form',
        name: 'form',
        routes: [
          {
            path: '/demo/form',
            redirect: '/demo/form/basic-form',
          },
          {
            name: 'basic-form',
            icon: 'smile',
            path: '/demo/form/basic-form',
            component: './demo/form/basic-form',
          },
          {
            name: 'step-form',
            icon: 'smile',
            path: '/demo/form/step-form',
            component: './demo/form/step-form',
          },
          {
            name: 'advanced-form',
            icon: 'smile',
            path: '/demo/form/advanced-form',
            component: './demo/form/advanced-form',
          },
        ],
      },
      {
        path: '/demo/list',
        icon: 'table',
        name: 'list',
        routes: [
          {
            path: '/demo/list/search',
            name: 'search-list',
            component: './demo/list/search',
            routes: [
              {
                path: '/demo/list/search',
                redirect: '/demo/list/search/articles',
              },
              {
                name: 'articles',
                icon: 'smile',
                path: '/demo/list/search/articles',
                component: './demo/list/search/articles',
              },
              {
                name: 'projects',
                icon: 'smile',
                path: '/demo/list/search/projects',
                component: './demo/list/search/projects',
              },
              {
                name: 'applications',
                icon: 'smile',
                path: '/demo/list/search/applications',
                component: './demo/list/search/applications',
              },
            ],
          },
          {
            path: '/demo/list',
            redirect: '/demo/list/table-list',
          },
          {
            name: 'table-list',
            icon: 'smile',
            path: '/demo/list/table-list',
            component: './demo/table-list',
          },
          {
            name: 'basic-list',
            icon: 'smile',
            path: '/demo/list/basic-list',
            component: './demo/list/basic-list',
          },
          {
            name: 'card-list',
            icon: 'smile',
            path: '/demo/list/card-list',
            component: './demo/list/card-list',
          },
        ],
      },
      {
        path: '/demo/profile',
        name: 'profile',
        icon: 'profile',
        routes: [
          {
            path: '/demo/profile',
            redirect: '/demo/profile/basic',
          },
          {
            name: 'basic',
            icon: 'smile',
            path: '/demo/profile/basic',
            component: './demo/profile/basic',
          },
          {
            name: 'advanced',
            icon: 'smile',
            path: '/demo/profile/advanced',
            component: './demo/profile/advanced',
          },
        ],
      },
      {
        name: 'result',
        icon: 'CheckCircleOutlined',
        path: '/demo/result',
        routes: [
          {
            path: '/demo/result',
            redirect: '/demo/result/success',
          },
          {
            name: 'success',
            icon: 'smile',
            path: '/demo/result/success',
            component: './demo/result/success',
          },
          {
            name: 'fail',
            icon: 'smile',
            path: '/demo/result/fail',
            component: './demo/result/fail',
          },
        ],
      },
      {
        name: 'exception',
        icon: 'warning',
        path: '/demo/exception',
        routes: [
          {
            path: '/demo/exception',
            redirect: '/demo/exception/403',
          },
          {
            name: '403',
            icon: 'smile',
            path: '/demo/exception/403',
            component: './demo/exception/403',
          },
          {
            name: '404',
            icon: 'smile',
            path: '/demo/exception/404',
            component: './demo/exception/404',
          },
          {
            name: '500',
            icon: 'smile',
            path: '/demo/exception/500',
            component: './demo/exception/500',
          },
        ],
      },
    ],
  },
  {
    path: '/account/center',
    component: './account/center',
  },
  {
    path: '/account/settings',
    component: './account/settings',
  },
  {
    path: '/',
    redirect: '/demo/dashboard/analysis',
  },
  {
    component: '404',
    path: '/*',
  },
];
