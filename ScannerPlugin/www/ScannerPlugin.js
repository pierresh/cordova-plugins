var exec = require('cordova/exec');
var ScannerPlugin = function (success,error) {
    return this;
};

ScannerPlugin.prototype = {
    init: function (successCallback, errorCallback, options) {
        exec(successCallback, errorCallback, "ScannerPlugin", "init", options);
    },
    /**
     * 扫描二维码/条形码
     * @param successCallback 成功回调
     * @param errorCallback 失败回调
     * @param options 参数
     */
    scan: function (successCallback, errorCallback, options) {
        exec(successCallback, errorCallback, "ScannerPlugin", "scan", options);
    }
}

if (!ScannerPlugin) {
    ScannerPlugin = new ScannerPlugin()
}
module.exports = new ScannerPlugin();