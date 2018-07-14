//
//  main.cpp
//  hellodjixcode
//  Created by liuchang on 2018/7/14.
//  Copyright © 2018年 orgname. All rights reserved.
//

#include <iostream>
#include "client_impl.hpp"
#include "client.grpc.pb.h"
#include "reply.hpp"

using namespace client;
using client::RegisterReply;


int main(int argc, const char * argv[]) {
    // insert code here...
    ClientImpl client = ClientImpl();
    Reply reply = client.register_account("liuchang", "123456", "deviceid");
    
    std::cout << "Hello, World!\n";
    
    return 0;
}
