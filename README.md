## Grpc + Djinni Register And Login Implementation


### Intro
- 一个Android/iOS客户端「单点登录注册」的练习。    
- 后台使用[Grpc-java](https://github.com/grpc/grpc-java)，数据库采用MySql Jdbc，中台使用[Djinni](https://github.com/dropbox/djinni)实现Android和iOS跨平台。
- [NOTICE]这是一个after-work project，目的是学习了两个框架的开发流程和实现关键逻辑。仍存在的问题详见「已知问题」。

![流程](https://github.com/LarryLawrence/RegisterAndLogin-Grpc-Djinni/blob/master/client/screenshots/general-process.png)  
1. 进入App后，先获取保存在native lib数据库中客户端保存的auth，通过rpc请求与后台对比，如果不一致，则跳转登录页；否则无需重新登录。
2. 登录时，如果deviceId发生变化，则生成新的auth，保存在server并返回给native lib保存。
3. 原本在App中的业务逻辑，应尽量移动到中间层native lib中来，方便移植、减少代码；比如客户端auth的存取，无需交给App来做。
4. Server端为3个接口定义了3种proto，3种返回实体；Native lib端作为桥接，使用djinni生成的抽象类定义了相应的接口，在其实现类中的每一个方法里，分别对server的这些接口做rpc请求。
   ```
   	// RPC请求
   	Status status = stub_->Register(&context, request, &registerReply);
   ```

### gRPC
使用[Grpc-java](https://github.com/grpc/grpc-java)，在```.proto```文件中一次定义```service```，就可以实现任何```gRPC```支持的语言的客户端和服务端，
于是就可以在包括Google服务器到你自己的平板电脑在内的环境上运行，在不同环境和不同语言之间进行通信的工作，都交给gPRC来搞定。由于采用了```protocol buffers```,同时也就
获取了包括高效序列化、简单IDL和简单的接口更新在内的各种优势。


### Djinni
用c++编写一次，就可以生成可以在Android和iOS上跨平台调用的可执行文件，相当于把gRPC的C++ Client放置在Djinni中做个中间层。

### Server Side
Server端采用```gprc-java```, 数据库采用```MySQL``` + ```JDBC```。提供3个接口:
1. ```checkAuth``` 检查client传给server的auth是否过期，比如是否因为deviceId变化而需要重新登录。
2. ```register``` 注册接口。
3. ```login``` 登录接口。


### Client Side
中台采用[Djinni](https://github.com/dropbox/djinni)。中台负责转发Android/iOS客户端的rpc请求，也可以做一些通用的操作，比如保存当前用户的auth。
编写djinni可以实现多端代码。前台只实现了Android端对动态链接库的调用范例(android_project folder)，iOS未实现。

### 安全性
##### 通信
```gRPC```本身提供了三种认证方式(详见：[Authentication](https://grpc.io/docs/guides/auth.html#supported-auth-mechanisms))，    
1. 使用client-side的SSL/TLS
2. 使用Google的基于token的认证
3. 扩展grpc的API实现你自己的认证方法

##### 密码保存
Server端密码保存方面，使用了[PBKDF2](https://en.wikipedia.org/wiki/PBKDF2)算法，我对它的salt做了改进。

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
│       ├── client_impl.cpp native方法具体实现
│       └── client_impl.hpp 头文件
└── screenshots 截图
    └── general-process.png
```
**server目录**：

```
.
└── grpc
    └── registerandlogin
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
        └── utils 
        │   └── DataBaseUtil.java 数据库CRUD的工具类
        │   └── EncryptionUtil.java.java 加解密工具类
        ├── CheckAuthProto.java 
        ├── CheckAuthReply.java 返回实体
        ├── CheckAuthReplyOrBuilder.java Builder接口
        ├── CheckAuthRequest.java 请求实体
        ├── CheckAuthRequestOrBuilder.java Builder接口
        ├── LoginProto.java
        ├── LoginReply.java 返回实体
        ├── LoginReplyOrBuilder.java
        ├── LoginRequest.java 请求实体
        ├── LoginRequestOrBuilder.java Builder接口
        ├── RegisterProto.java 
        ├── RegisterReply.java 返回实体
        ├── RegisterReplyOrBuilder.java Builder接口
        ├── RegisterRequest.java 请求实体
        └── RegisterRequestOrBuilder.java Builder接口
```


### 已知问题
- ~~（国内网络环境无法clone/submodule https://chromium.googlesource.com/external/gyp.git，    
使用vpn亦不行，可能dns server被墙）~~ 已上传
- make android 提示找不到grpcpp.h，但是include文件夹里有



![app](https://github.com/LarryLawrence/RegisterAndLogin-Grpc-Djinni/blob/master/client/screenshots/app-screen.png)

------

-July 2018 