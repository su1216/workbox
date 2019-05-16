## Requirements

Android 4.4+ (API level 19+)



如果想在低版本中使用workbox，请在Manifest.xml中添加如下代码:

```
<uses-sdk tools:overrideLibrary="com.su.workbox" />
```

部分功能在低版本手机中将无法使用




## Workbox

```groovy
debugApi 'com.su:workbox.annotations:0.8.7'
debugAnnotationProcessor 'com.su:workbox.compiler:0.8.7'
debugImplementation 'com.su:workbox:0.8.7'
releaseImplementation 'com.su:workbox-no-op:0.8.7'

```

需要将module名称传给compiler

```groovy
defaultConfig {
    ...
    javaCompileOptions {
        annotationProcessorOptions {
            arguments = [MODULE_NAME: project.getName()]
        }
    }
}
```

您可以通过实现自己的`com.su.workbox.WorkboxSupplier`来改变策略。

初始化workbox

```java
private static void initWorkbox(Application application) {
    Workbox.init(application, "您的WorkboxSupplier类名");
}
```

收集崩溃日志

需要将CrashLogHandler设置为DefaultUncaughtExceptionHandler

```java
if (BuildConfig.DEBUG) {
    Thread.setDefaultUncaughtExceptionHandler(Workbox.newLogUncaughtExceptionHandler(true));
}
```

或者，在您的UncaughtExceptionHandler中调用CrashLogHandler其`uncaughtException`方法

根据需要设置是否杀死进程




模拟activity传参跳转时，可以对activity进行如下类似配置

```java
@NoteComponent(description = "页面传参测试",
        type = "activity",
        parameters = {@Parameter(parameterName = "object", parameterClass = ObjectParameter.class, parameterRequired = false),
                @Parameter(parameterName = "objects", parameterClass = ObjectParameter[].class, parameterRequired = false),
                @Parameter(parameterName = "int", parameterClass = int.class),
                @Parameter(parameterName = "long", parameterClass = long.class, parameterRequired = false)})
```

其中`type`必须要指定为`activity`，参数不仅可以配置为基本数据类型也可以配置实现Parcelable的类型。

启动service也类似。



其中`getRequestBodyExcludeKeys`是下文提到的过滤特定的请求字段。

> {
> ​	"body": {
> ​		"random": "abc"
> ​	},
> ​	"traceId": "efg"
> }

例如，当您想过滤请求体中body字段下的random字段和traceId字段时，需要按如下格式覆盖此函数：

```java
@NonNull
@Override
public List<List<String>> getRequestBodyExcludeKeys() {
    List<List<String>> keys = new ArrayList<>();
    List<String> random = new ArrayList<>();
    random.add("body");
    random.add("random");
    keys.add(random);
    List<String> traceId = new ArrayList<>();
    traceId.add("traceId");
    keys.add(traceId);
    return keys;
}
```

在使用域名切换时，可以覆盖`urlMapping`来实现自己的url映射策略

使用`Server相关`的功能时，需要给OkHttp添加如下拦截器

```java
Object hostInterceptor = Workbox.getHostInterceptor();
if (hostInterceptor != null) {
	builder.addInterceptor((Interceptor) hostInterceptor);
}
Object mockInterceptor = Workbox.getMockInterceptor();
if (mockInterceptor != null) {
	builder.addInterceptor((Interceptor) mockInterceptor);
}
Object dataCollectorInterceptor = Workbox.getDataCollectorInterceptor();
if (dataCollectorInterceptor != null) {
	builder.addInterceptor((Interceptor) dataCollectorInterceptor);
}
Object dataUsageInterceptor = Workbox.getDataUsageInterceptorInterceptor();
if (dataUsageInterceptor != null) {
	builder.addInterceptor((Interceptor) dataUsageInterceptor);
}
```

`HostInterceptor`用于域名切换

`MockInterceptor`用于数据模拟

`DataCollectorInterceptor`用于自动收集数据

`DataUsageInterceptor`用于自动统计流量

在WebView中切换域名时需要集成如下代码：

```java
String host = Workbox.getWebViewHost();
if (!TextUtils.isEmpty(host)) {
    mUrl = Workbox.urlMapping(mUrl, host);
}


```

更多细节请参阅[wiki](https://github.com/su1216/workbox/wiki)



## 功能列表




![](images/entry.jpg)

可以通过上面的开关给调试模块在桌面上添加入口。
