 # 本地推送

手机本地推送

安装 `LocalNotification`
 

``` shell
 cordova plugin add LocalNotification
```

## 使用

传入数据以json形式传入，点击通知后数据通过callBack以json形式传出

``` js
declare let LocalNotification :any;
...
  // 注册推送
  LocalNotification.showNotification(function(msg) {
      console.log(msg);
      var title = msg.title;
      var text = msg.text
      alert('success:' + msg);
  }, function(err) {
      console.log(err);
      alert('fail:' + err);
  }, 
    {
        title:'title',
        text:'text'
    }
  );

```

也可以定义事件onLocalNotificationClickd
```js
document.addEventListener('onLocalNotificationClickd', onLocalNotificationClickd, false);

function onLocalNotificationClickd(msg){
    console.log(msg.text);
}
```

使用那处处理可以自行选择，建议只用一处处理

