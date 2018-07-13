// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client.djinni

#pragma once

#include <cstdint>
#include <string>
#include <utility>

namespace client {

struct Reply final {
    int32_t resultcode;
    std::string resultmsg;
    bool isauthvalid;
    std::string auth;

    Reply(int32_t resultcode_,
          std::string resultmsg_,
          bool isauthvalid_,
          std::string auth_)
    : resultcode(std::move(resultcode_))
    , resultmsg(std::move(resultmsg_))
    , isauthvalid(std::move(isauthvalid_))
    , auth(std::move(auth_))
    {}
};

}  // namespace client
