 # ScannerPlugin

类微信扫码功能

安装 `ScannerPlugin`
 

``` shell
 cordova plugin add {Your local path}/ScannerPlugin
```

## 使用

使用前需初始化OpenCV

``` js
declare let ScannerPlugin :any;
...
  // 初始化
  ScannerPlugin.init(function(res) {
      console.log(res);
      alert('init success:' + res);
  }, function(err) {
      console.log(err);
      alert('init fail:' + err);
  }, []);

// 开始扫描
ScannerPlugin.scan(function(res) {
    console.log(res); // 扫描结果
}, function(err) {
      console.log(err);
},[]);
```

