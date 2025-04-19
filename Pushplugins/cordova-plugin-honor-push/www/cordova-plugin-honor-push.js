var exec = require('cordova/exec');
var HonorPush = function (success, error) {
    return this;
};

HonorPush.prototype = {
    /**
     * 注册推送服务
     * @param successCallback 成功回调
     * @param errorCallback 失败回调
     * @param options 参数
     */
    register: function (successCallback, errorCallback, options) {
        exec(successCallback, errorCallback, "HonorPush", "register", options);
    },
    /**
    * 当token变化后，会触发方法的successCallback回调
 * @param successCallback token被自动变更时通知变更后的token
 * @param errorCallback 通知失败的回调
 */
    onNewToken: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, "HonorPush", "onNewToken", []);
    }
}

if (!HonorPush) {
    HonorPush = new HonorPush()
}

module.exports = new HonorPush();
