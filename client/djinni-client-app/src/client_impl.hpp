#pragma once

#include <cstdint>
#include <memory>
#include <string>
#include <vector>
#include "../generated-src/cpp/reply.hpp"
#include "../generated-src/cpp/user.hpp"
#include "../generated-src/cpp/client_interface.hpp"


namespace client {

    class ClientImpl : public ClientInterface {

    public:
        // Constructor
//        ClientImpl(const std::string &path, std::string &deviceid);
        ClientImpl();
        // Database functions we need to implement in C++
//        std::vector<Todo> get_todos();
//        int32_t add_todo(const std::string & label);
//        bool update_todo_completed(int32_t id, int32_t completed);
//        bool delete_todo(int32_t id);

//        static create_with_path(path: string): client_interface;
//        get_userinfo(username: string): user;
//        register(username: string, password: string, deviceid: string): reply;
//        check_auth(username: string, auth: string): reply;
//        login(username: string, password: string, deviceid: string): reply;
        User get_userinfo(const std::string &username);

        Reply check_auth(const std::string &username, const std::string &auth);

        Reply register_account(const std::string &username, const std::string &password, const std::string &deviceid);

        Reply login(const std::string &username, const std::string &password, const std::string &deviceid);


    private:
        void _setup_db();

        void _handle_query(std::string query);
    };

}
