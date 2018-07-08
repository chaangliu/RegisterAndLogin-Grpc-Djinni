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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

        //注册
        @Override
        public void register(RegisterRequest req, StreamObserver<RegisterReply> responseObserver) {
            final int RESULT_SUCCESS = 0;
            final int RESULT_FAIL = -1;
            String userName = req.getUserName();
            String userPwd = req.getUserPwd();
            String deviceId = req.getDeviceId();
            String queriedUserName = DataBaseUtil.query(userName);
            String queriedDeviceId = DataBaseUtil.query(deviceId);
            String queriedPwd = DataBaseUtil.query(userPwd);
            LoginReply reply = null;
            if (queriedUserName == null || queriedUserName.length() == 0) {
                //userName不存在
                RegisterReply.newBuilder().setResultCode(RESULT_FAIL).setResultMsg("用户名不存在").build();

            } else if (!userPwd.equals(queriedPwd)) {
                //用户名密码不匹配
                RegisterReply.newBuilder().setResultCode().setResultMsg("用户名与密码不匹配").build();

            } else {
                //用户名密码匹配，deviceId不同，更新auth
                RegisterReply.newBuilder().setResultCode(RESULT_SUCCESS).setAuth("<newAuth>").setResultMsg("注册成功").build();
            }
            //回调结果
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    static class CheckAuthImpl extends CheckAuthGrpc.CheckAuthImplBase {
        //注册
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

        //注册
        @Override
        public void login(LoginRequest req, StreamObserver<LoginReply> responseObserver) {
            final int RESULT_SUCCESS = 0;
            final int RESULT_FAIL = -1;
            String userName = req.getUserName();
            String userPwd = req.getUserPwd();
            String deviceId = req.getDeviceId();
            String queriedUserName = DataBaseUtil.query(userName);
            String queriedDeviceId = DataBaseUtil.query(deviceId);
            String queriedPwd = DataBaseUtil.query(userPwd);
            LoginReply reply = null;

            if (queriedUserName == null || queriedUserName.length() == 0) {
                //userName不存在
                LoginReply.newBuilder().setResultCode(RESULT_FAIL).setResultMsg("用户名不存在").build();

            } else if (!userPwd.equals(queriedPwd)) {
                //用户名密码不匹配
                LoginReply.newBuilder().setResultCode().setResultMsg("用户名与密码不匹配").build();

            } else {
                //用户名密码匹配，deviceId不同，更新auth
                LoginReply.newBuilder().setResultCode(RESULT_SUCCESS).setAuth("<newAuth>").setResultMsg("注册成功").build();
            }
            //回调结果
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
