/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.registerandlogin.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.registerandlogin.*;
import io.grpc.registerandlogin.grpc.CheckAuthGrpc;
import io.grpc.registerandlogin.utils.DataBaseUtil;
import io.grpc.registerandlogin.utils.EncryptionUtil;
import io.grpc.stub.StreamObserver;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

import static io.grpc.registerandlogin.utils.DataBaseUtil.*;

public class RegisterAndLoginServer {
    private static final Logger logger = Logger.getLogger(RegisterAndLoginServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new RegisterImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                RegisterAndLoginServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final RegisterAndLoginServer server = new RegisterAndLoginServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class RegisterImpl extends RegisterGrpc.RegisterImplBase {
        /**
         * 注册
         *
         * @param req              请求实体
         * @param responseObserver 响应观察者
         */
        @Override
        public void register(RegisterRequest req, StreamObserver<RegisterReply> responseObserver) {
            final int RESULT_SUCCESS = 0;
            final int RESULT_FAILURE = -1;
            String userName = req.getUserName();
            String userPwd = req.getUserPwd();
            String deviceId = req.getDeviceId();
            String queriedUserName = DataBaseUtil.query(userName, COLUMN_USER_NAME);
            RegisterReply reply;
            if (queriedUserName != null) {
                //userName已存在
                reply = RegisterReply.newBuilder().setResultCode(RESULT_FAILURE).setResultMsg("用户名已经存在，请换一个用户名").build();

            } else if (!userPwd.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$")) {
                //密码不符合规则
                reply = RegisterReply.newBuilder().setResultCode(RESULT_FAILURE).setResultMsg("密码不符合规则").build();

            } else {
                //注册成功
                String auth = generateAuth(userName, deviceId);
                reply = RegisterReply.newBuilder().setResultCode(RESULT_SUCCESS).setAuth(auth).setResultMsg("注册成功").build();
                //addEntry中对密码做加密处理
                DataBaseUtil.addEntry(userName, userPwd, deviceId, auth);
            }
            //回调结果
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    static class CheckAuthImpl extends CheckAuthGrpc.CheckAuthImplBase {
        /**
         * 检查auth
         *
         * @param req              请求实体
         * @param responseObserver 响应观察者
         */
        @Override
        public void checkAuth(CheckAuthRequest req, StreamObserver<CheckAuthReply> responseObserver) {
            String userName = req.getUserName();
            String auth = req.getAuth();
            String queriedAuth = DataBaseUtil.query(userName, COLUMN_AUTH);
            CheckAuthReply reply;
            //用户名对应的auth
            if (!queriedAuth.equals(auth)) {
                reply = CheckAuthReply.newBuilder().setIsValid(false).build();
            } else {
                //auth仍有效，无需登录
                reply = CheckAuthReply.newBuilder().setIsValid(true).build();
            }
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    static class LoginImpl extends io.grpc.registerandlogin.grpc.LoginGrpc.LoginImplBase {
        /**
         * 登录
         *
         * @param req              请求实体
         * @param responseObserver 相应观察者
         */
        @Override
        public void login(LoginRequest req, StreamObserver<LoginReply> responseObserver) {
            final int RESULT_SUCCESS = 0;
            final int RESULT_FAILURE = -1;
            String userName = req.getUserName();
            String userPwd = req.getUserPwd();
            String deviceId = req.getDeviceId();
            String queriedUserName = DataBaseUtil.query(userName, COLUMN_PASSWORD);
            String queriedPwd = DataBaseUtil.query(userName, COLUMN_PASSWORD);
            LoginReply reply = null;

            if (queriedUserName == null || queriedUserName.length() == 0) {
                //用户名不存在
                reply = LoginReply.newBuilder().setResultCode(RESULT_FAILURE).setResultMsg("用户名不存在").build();
            } else {
                try {
                    if (!EncryptionUtil.authenticate(userPwd, queriedPwd, EncryptionUtil.generateSalt(userPwd))) {
                        //用户名密码不匹配
                        reply = LoginReply.newBuilder().setResultCode(RESULT_FAILURE).setResultMsg("用户名与密码不匹配").build();
                    } else {
                        //用户名密码匹配
                        String newAuth = generateAuth(userName, deviceId);
                        reply = LoginReply.newBuilder().setResultCode(RESULT_SUCCESS).setAuth(newAuth).setResultMsg("登录成功").build();
                        //更新数据库中的auth
                        DataBaseUtil.updateAuth(userName, newAuth);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //回调结果
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    /**
     * 生成auth
     *
     * @param userName 用户名
     * @param deviceId 设备id
     * @return auth
     */
    private static String generateAuth(String userName, String deviceId) {
        return encoderByMd5(userName + deviceId);
    }

    private static String encoderByMd5(String str) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64en = new BASE64Encoder();
            try {
                return base64en.encode(md5.digest(str.getBytes("utf-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
