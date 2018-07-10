#include "todo_list_impl.hpp"
#include <iostream>
#include <string>
#include <vector>
#include <sqlite3.h>

#include <memory>
#include <grpcpp/grpcpp.h>

#ifdef BAZEL_BUILD
#include "examples/protos/loginclient.grpc.pb.h"
#else
#include "loginclient.grpc.pb.h"
#endif

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;
using loginclient::CheckAuthRequest;
using loginclient::LoginRequest;
using loginclient::RegisterRequest;
// using loginclient::CheckAuthReply;
// using loginclient::LoginReply;
// using loginclient::RegisterReply;
  
namespace client {
    
    std::string _path;
    sqlite3 *db;
    char *zErrMsg = 0;
    int rc;
    std::string sql;
    sqlite3_stmt *statement;
    // LoginClient(std::shared_ptr<Channel> channel): stub_(Login::NewStub(channel)) {}
    std::shared_ptr<Client> Client::create_with_path(const std::string & path) {
        return std::make_shared<ClientImpl>(path);
    }
    
    ClientImpl::ClientImpl(const std::string & path) {
        _path = path + "/userdata.db";
        _setup_db();
    }

    //验证auth接口，进入App后登录之前先调用auth验证，判断是否需要重新登录(比如被挤掉或过期)
    std::Reply ClientImpl::check_auth(const std::string & username, string & auth) {
        
        //把用户名和客户端存放的auth传递给server
        CheckAuthRequest request;
        request.set_name(username);
        request.set_auth(auth);

        //server返回的reply的container
        std::Reply reply;

        //给client的context，可以用来传递额外信息给server或者调整特定的RPC行为
        ClientContext context;

        // RPC过程
        Status status = stub_->CheckAuth(&context, request, &reply);

        // 对status的操作
        if (status.ok()) {
          //将结果透传给app
          return reply;
        } else {
          std::cout << status.error_code() << ": " << status.error_message()
                    << std::endl;
          std::string resultmsg = "RPC 失败";
          int32_t resultcode = -1;
          bool isauthvalid = 0;
          reply = {
            resultcode,
            resultmsg,
            isauthvalid
          }
          return reply;
        }
    }

    //注册
    std::Reply ClientImpl::register(const std::string& userName, string& userPwd, String& deviceId) {
        RegisterRequest request;
        request.set_name(userName);
        request.set_pwd(userPwd);
        request.set_deviceId(deviceId);

        std::Reply reply;

        ClientContext context;

        // RPC请求
        Status status = startRegisterRpc(&context, request, &reply);

        if (status.ok()) {
          //透传给app
          return reply;
        } else {
          std::cout << status.error_code() << ": " << status.error_message()
                    << std::endl;
          int32_t resultcode = -1;
          std::string resultmsg = "RPC 失败";
          bool isauthvalid = 0;
          std::string auth = "";
          reply = {
            resultcode,
            resultmsg,
            isauthvalid,
            auth
          }
          return reply;
        }
    }

    //登录
    std::Reply ClientImpl::login(const std::string& userName, string& userPwd, String& deviceId) {
        LoginRequest request;
        request.set_name(userName);
        request.set_pwd(userPwd);
        request.set_deviceId(deviceId);

        std::Reply reply;

        ClientContext context;
        // RPC请求
        Status status = stub_->Login(&context, request, &reply);

        if (status.ok()) {
          //透传给app
          return reply;
        } else {
          std::cout << status.error_code() << ": " << status.error_message()
                    << std::endl;
          int32_t resultcode = -1;
          std::string resultmsg = "RPC 失败";
          bool isauthvalid = 0;
          std::string auth = "";
          reply = {
            resultcode,
            resultmsg,
            isauthvalid,
            auth
          }
          return reply;
        }
    }

    std::Reply startRegisterRpc(ClientContext context, request, Reply reply) {
          LoginClient loginclient(grpc::CreateChannel("localhost:50051", grpc::InsecureChannelCredentials()));
          std::string reply = loginclient.register(context, request, reply);

          auto channel_creds = grpc::SslCredentials(grpc::SslCredentialsOptions());
          auto channel = grpc::CreateChannel("localhost:50051", creds);
          std::unique_ptr<Greeter::Stub> stub(Greeter::NewStub(channel));
    }
  
    //获取用户信息，返回User实体
    std::User ClientImpl::get_userinfo(string username) {
        
        std::User targetUser;
        
        // 获取所有records
        sql = "SELECT * FROM users";
        if(sqlite3_prepare_v2(db, sql.c_str(), sql.length()+1, &statement, 0) == SQLITE_OK) {
            int result = 0;
            while(true) {
                result = sqlite3_step(statement);
                if(result == SQLITE_ROW) {

                    int32_t id = sqlite3_column_int(statement, 0);
                    std::string username = (char*)sqlite3_column_text(statement, 1);
                    std::string password = (char*)sqlite3_column_text(statement, 2);
                    std::string auth = (char*)sqlite3_column_text(statement, 3);   
                    std::string deviceid = (char*)sqlite3_column_text(statement, 4);   
                    targetUser = {
                        id,
                        username,
                        password,
                        auth,
                        deviceid
                    };
                } else {
                    break;
                }
            }
            sqlite3_finalize(statement);

        } else {
            auto error = sqlite3_errmsg(db);
            if (error!=nullptr) printf("Error: %s", error);
            else printf("Unknown Error");
        }
        
        return targetUser;
    }
    
    // wrapper to handle errors, etc on simple queries
    void ClientImpl::_handle_query(std::string sql) {
        rc = sqlite3_exec(db, sql.c_str(), 0, 0, &zErrMsg);
        if(rc != SQLITE_OK){
            fprintf(stderr, "SQL error: %s\n", zErrMsg);
            sqlite3_free(zErrMsg);
            return;
        }
    }
    
    void ClientImpl::_setup_db() {
        
        // 打开database，如果有需要就创建一个
        rc = sqlite3_open_v2(_path.c_str(), &db, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, NULL);
        if(rc){
            fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
            sqlite3_close(db);
            return;
        }
        
        // 如果table不存在就创建一个
        sql = "CREATE TABLE IF NOT EXISTS todos("  \
            "id INTEGER PRIMARY KEY AUTOINCREMENT    NOT NULL," \
            "username          TEXT    NOT NULL," \
            "password          TEXT    NOT NULL," \
            "auth          TEXT    NOT NULL," \
            "deviceid         INT     NOT NULL);";
        _handle_query(sql);
        
        // 判断table是否是空，可以初始化一些数据
        sql = "SELECT * FROM users";
        _handle_query(sql);
        if(sqlite3_prepare_v2(db, sql.c_str(), sql.length()+1, &statement, 0) == SQLITE_OK) {
            int stat = sqlite3_step(statement);
            if (stat == SQLITE_DONE) {
                // 可以初始化数据
                sql = "INSERT INTO users (username, "") ";
                _handle_query(sql);
            } else {
                std::cout << "didn't add data to db\n";
            }
        } else {
            int error = sqlite3_step(statement);
            std::cout << "SQLITE not ok, error was " << error << "\n";
            
        }
        sqlite3_finalize(statement);
        
    }
  
}