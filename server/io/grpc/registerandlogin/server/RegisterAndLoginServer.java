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
import io.grpc.stub.StreamObserver;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

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
         * @param req 请求实体
         * @param responseObserver 响应观察者
         */
        @Override
        public void register(RegisterRequest req, StreamObserver<RegisterReply> responseObserver) {
            final int RESULT_SUCCESS = 0;
            final int RESULT_FAILURE = -1;
            String userName = req.getUserName();
            String userPwd = req.getUserPwd();
            String deviceId = req.getDeviceId();
            String queriedUserName = DataBaseUtil.query(userName);
            RegisterReply reply = null;
            if (queriedUserName != null) {
                //userName已存在
                RegisterReply.newBuilder().setResultCode(RESULT_FAILURE).setResultMsg("用户名已经存在，请换一个用户名").build();

            } else if (!userPwd.matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$")) {
                //用户名密码不匹配
                RegisterReply.newBuilder().setResultCode(RESULT_FAILURE).setResultMsg("密码不不符合规则").build();

            } else {
                //用户名密码匹配，deviceId不同，更新auth
                RegisterReply.newBuilder().setResultCode(RESULT_SUCCESS).setAuth(generateAuth(userName, deviceId)).setResultMsg("注册成功").build();
            }
            //回调结果
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    static class CheckAuthImpl extends CheckAuthGrpc.CheckAuthImplBase {
        /**
         * 检查auth
         * @param req 请求实体
         * @param responseObserver 响应观察者
         */
        @Override
        public void checkAuth(CheckAuthRequest req, StreamObserver<CheckAuthReply> responseObserver) {
            String userName = req.getUserName();
            String auth = req.getAuth();
            String queriedAuth = DataBaseUtil.query(userName);
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
         * @param req 请求实体
         * @param responseObserver 相应观察者
         */
        @Override
        public void login(LoginRequest req, StreamObserver<LoginReply> responseObserver) {
            final int RESULT_SUCCESS = 0;
            final int RESULT_FAILURE = -1;
            String userName = req.getUserName();
            String userPwd = req.getUserPwd();
            String deviceId = req.getDeviceId();
            String queriedUserName = DataBaseUtil.query(userName);
            String queriedDeviceId = DataBaseUtil.query(deviceId);
            String queriedPwd = DataBaseUtil.query(userPwd);
            LoginReply reply;

            if (queriedUserName == null || queriedUserName.length() == 0) {
                //userName不存在
                reply = LoginReply.newBuilder().setResultCode(RESULT_FAILURE).setResultMsg("用户名不存在").build();

            } else if (!userPwd.equals(queriedPwd)) {
                //用户名密码不匹配
                reply = LoginReply.newBuilder().setResultCode(RESULT_FAILURE).setResultMsg("用户名与密码不匹配").build();

            } else {
                //用户名密码匹配，deviceId不同，更新auth
                reply = LoginReply.newBuilder().setResultCode(RESULT_SUCCESS).setAuth(generateAuth(userName, deviceId)).setResultMsg("注册成功").build();
            }
            //回调结果
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    /**
     * 生成auth
     * @param userName 用户名
     * @param deviceId 设备id
     * @return auth
     */
    private static String generateAuth(String userName, String deviceId) {
        return encoderByMd5(userName + deviceId + System.currentTimeMillis());
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
