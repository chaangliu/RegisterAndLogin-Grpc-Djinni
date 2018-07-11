## Grpc + Djinni Register And Login Implementation


### Intro
- 一个Android/iOS客户端「单点登录注册」的练习。    
- 后台使用[Grpc-java](https://github.com/grpc/grpc-java)，数据库采用MySql Jdbc，中台使用[Djinni](https://github.com/dropbox/djinni)实现Android和iOS跨平台。
- [NOTICE]由于是一个短时间的after-work project，功能并未完全跑通，只实现了关键逻辑以及学习了两个框架的开发流程，作为参考。详见「已知问题」。

![流程](https://github.com/LarryLawrence/RegisterAndLogin-Grpc-Djinni/blob/master/client/screenshots/general-process.png)  
1. 进入App后，先获取保存在native lib数据库中客户端保存的auth，通过rpc请求与后台对比，如果不一致，则跳转登录页；否则无需重新登录。
2. 登录时，如果deviceId发生变化，则生成新的auth，保存在server并返回给native lib保存。

### gRPC
使用[Grpc-java](https://github.com/grpc/grpc-java)，在```.proto```文件中一次定义```service```，就可以实现任何```gRPC```支持的语言的客户端和服务端，
于是就可以在包括Google服务器到你自己的平板电脑在内的环境上运行，在不同环境和不同语言之间进行通信的工作，都交给gPRC来搞定。由于采用了```protocol buffers```,同时也就
获取了包括高效序列化、简单IDL和简单的接口更新在内的各种优势。


### Djinni
用c++编写一次，就可以生成可以在Android和iOS上跨平台调用的可执行文件。

### Server Side
```grpc-go```的依赖被墙严重，故Server端采用```gprc-java```, 数据库采用```MySQL``` + ```JDBC```。提供4个接口:
1. ```getUserInfo``` 获取用户信息，返回用户信息实体。
2. ```checkAuth``` 检查client传给server的auth是否过期，比如是否因为deviceId变化而需要重新登录。
3. ```register``` 注册接口。
4. ```login``` 登录接口。

### Client Side
中台采用[Djinni](https://github.com/dropbox/djinni)。中台负责转发Android/iOS客户端的rpc请求，也可以做一些通用的操作，比如保存当前用户的auth。
编写djinni可以实现多端代码。前台只实现了Android端对动态链接库的调用范例(android_project folder)，iOS未实现。

### 安全通信
```gRPC```本身提供了三种认证方式(详见：[Authentication](https://grpc.io/docs/guides/auth.html#supported-auth-mechanisms))，    
这个项目中使用了第一种。
1. 使用client-side的SSL/TLS
2. 使用Google的基于token的认证
3. 扩展grpc的API实现你自己的认证方法

### 目录结构
**client目录**：
```
├── djinni-client-app
│   ├── GypAndroid.mk
│   ├── Makefile
│   ├── README.md
│   ├── android_project
│   │   └── AndroidClientApp 安卓客户端UI
│   ├── client.djinni 定义的供客户端调用的方法和接口
│   ├── deps 各项依赖
│   │   ├── djinni
│   │   ├── gyp
│   │   ├── sqlite3
│   │   └── sqlite3.gyp
│   ├── generated-src 根据编写的djinni、gyp生成的文件
│   │   ├── cpp
│   │   ├── java
│   │   ├── jni
│   │   └── objc
│   ├── libclientapp.gyp gyp文件
│   ├── libclientapp_jni.target.mk
│   ├── run_djinni.sh 运行脚本
│   └── src
│       ├── client_impl.cpp 原生方法具体实现
│       └── client_impl.hpp 头文件
└── screenshots 截图
    └── general-process.png
```
**server目录**：

```
.
└── grpc
    └── registerandlogin
        ├── generated 根据proto文件生成的封装好的请求/返回实体、Proto、Builder文件等
        │   ├── CheckAuthProto.java 
        │   ├── CheckAuthReply.java 返回实体
        │   ├── CheckAuthReplyOrBuilder.java Builder接口
        │   ├── CheckAuthRequest.java 请求实体
        │   ├── CheckAuthRequestOrBuilder.java Builder接口
        │   ├── LoginProto.java
        │   ├── LoginReply.java 返回实体
        │   ├── LoginReplyOrBuilder.java
        │   ├── LoginRequest.java 请求实体
        │   ├── LoginRequestOrBuilder.java Builder接口
        │   ├── RegisterProto.java 
        │   ├── RegisterReply.java 返回实体
        │   ├── RegisterReplyOrBuilder.java Builder接口
        │   ├── RegisterRequest.java 请求实体
        │   └── RegisterRequestOrBuilder.java Builder接口
        ├── grpc 根据proto文件生成的，供给/server目录下具体实现类使用的Stub类
        │   ├── CheckAuthGrpc.java 
        │   ├── LoginGrpc.java
        │   └── RegisterGrpc.java
        ├── proto 定义接口和实体(请求实体、返回实体)的proto文件
        │   ├── check_auth.proto
        │   ├── login.proto
        │   └── register.proto
        ├── server 真正的server功能实现类，可在request到来时做想要的处理并且回调reply
        │   └── RegisterAndLoginServer.java 
        └── utils 数据库创建查询的帮助类
            └── DataBaseUtil.java
```


### 已知问题
1. 国内网络环境无法clone/submodule https://chromium.googlesource.com/external/gyp.git，使用vpn亦不行，可能dns server被墙
2. 由于我C++不是很熟悉，djinni-client-app/src/client_impl.cpp 写得可能有些问题
3. 尽量使用brew install，可以省去配置成本。


![app](https://github.com/LarryLawrence/RegisterAndLogin-Grpc-Djinni/blob/master/client/screenshots/app-screen.png)

------

-July 2018 