(function () {
  window.CC = window.CC || {};
  window.CC.config = {
    apiBaseUrl: '',
    loginPath: '/pages/auth/login.html',
    homePath: '/index.html',
    adminPath: '/pages/admin/dashboard.html',
    tokenStorageKey: 'cc.jwt',
    emailStorageKey: 'cc.email',
    authReasonQueryKey: 'reason'
  };
})();
