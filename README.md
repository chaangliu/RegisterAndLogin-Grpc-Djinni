## Grpc + Djinni Register And Login Implementation


### Intro
- 一个Android/iOS客户端「单点登录注册」的练习。    
- 后台使用[Grpc-java](https://github.com/grpc/grpc-java)，数据库采用MySql Jdbc，中台使用[Djinni](https://github.com/dropbox/djinni)实现Android和iOS跨平台。
- [NOTICE]由于初次接触，时间亦只有不到一周的业余时间，功能并未完全跑通，只是实现了一些关键逻辑，
以及学习了两个框架的开发流程，作为参考。详见结尾「已知问题」。

### gRPC
使用[Grpc-java](https://github.com/grpc/grpc-java)，在```.proto```文件中一次定义```service```，就可以实现任何```gRPC```支持的语言的客户端和服务端，
于是就可以在包括Google服务器到你自己的平板电脑在内的环境上运行，在不同环境和不同语言之间进行通信的工作，都交给gPRC来搞定。由于采用了```protocol buffers```,同时也就
获取了包括高效序列化、简单IDL和简单的接口更新在内的各种优势。


### Djinni
用c++编写一次，就可以生成可以在Android和iOS上跨平台调用的可执行文件。

### Server Side
```grpc-go```的依赖被墙严重，故Server端采用```gprc-java```, 数据库采用```MySQL``` + ```JDBC```。提供三个接口，
1. ```checkAuth``` 检查client传给server的auth是否过期，比如是否因为deviceId变化而需要重新登录。
2. ```register``` 注册接口。
3. ```login``` 登录接口。


### Client Side
中台采用[Djinni](https://github.com/dropbox/djinni)。前台只实现了Android端对动态链接库的调用范例，iOS未实现。

### 安全通信
```grpc```本身提供了三种认证方式(详见：[Authentication](https://grpc.io/docs/guides/auth.html#supported-auth-mechanisms))，    
这个项目中使用了第一种。
1. 使用client-side的SSL/TLS
2. 使用Google的基于token的认证
3. 扩展grpc的API实现你自己的认证方法
