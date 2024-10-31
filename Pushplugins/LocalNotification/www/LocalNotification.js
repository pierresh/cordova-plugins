var exec = require('cordova/exec');

var LocalNotification = function (success, error) {
    return this;
};

LocalNotification.prototype = {
    /**
     * 注册推送服务
     * @param successCallback 成功回调
     * @param errorCallback 失败回调
     * @param options 参数
     */
    showNotification: function (successCallback, errorCallback, options) {
        exec(successCallback, errorCallback, "LocalNotification", "showNotification", [options]);
    },
}

if (!LocalNotification) [
    LocalNotification = new LocalNotification()
]

module.exports = new LocalNotification();
