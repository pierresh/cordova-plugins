 # Vivo推送

Vivo手机推送

安装 `cordova-plugin-vivo-push`
 

``` shell
 cordova plugin add cordova-plugin-vivo-push --variable  APP_KEY=YOUR_APP_KEY --variable APP_ID=YOUR_APP_ID
```

## 使用

使用前需注册，以获取 `token` ，你可以将 `token` 与你的app用户信息关联后上传到服务器

``` js
declare let VivoPush :any;
...
  // 注册推送
  VivoPush.register(function(token) {
      console.log(token);
      alert('register success:' + token);
  }, function(err) {
      console.log(err);
      alert('register fail:' + err);
  }, []);

// 接收token
VivoPush.onNewToken(function(token) {
    console.log(token); // 会多次接收到token
}, function(err) {
      console.log(err);
      alert('register fail:' + err);
});
```

注册完成后，需要监听 `messageReceived` 事件

``` js
document.addEventListener("messageReceived", function(result) {
    console.log(result);
}, false);
```
