 # ScannerPlugin

类微信扫码功能

安装 `ScannerPlugin`
 

``` shell
 cordova plugin add ScannerPlugin
```

## 使用

使用前需初始化OpenCV

``` js
declare let ScannerPlugin :any;
...
  // 初始化
  ScannerPlugin.init(function(token) {
      console.log(token);
      alert('register success:' + token);
  }, function(err) {
      console.log(err);
      alert('register fail:' + err);
  }, []);

// 开始扫描
ScannerPlugin.scan(function(token) {
    console.log(token); // 会多次接收到token
}, function(err) {
      console.log(err);
      alert('register fail:' + err);
},[]);
```

