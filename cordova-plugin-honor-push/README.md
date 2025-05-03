 # Honor

Honor手机推送

安装 `cordova-plugin-honor-push`
 

``` shell
 cordova plugin add cordova-plugin-honor-push --variable APP_ID=YOUR_APP_ID
```

## 使用

使用前需注册，以获取 `token` ，你可以将 `token` 与你的app用户信息关联后上传到服务器

``` js
declare let HonorPush :any;
...
  // 注册推送
  HonorPush.register(function(token) {
      console.log(token);
      alert('register success:' + token);
  }, function(err) {
      console.log(err);
      alert('register fail:' + err);
  }, []);

// 接收token
HonorPush.onNewToken(function(token) {
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
