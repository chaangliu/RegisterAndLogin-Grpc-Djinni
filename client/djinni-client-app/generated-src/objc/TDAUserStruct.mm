// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client.djinni

#import "TDAUserStruct.h"


@implementation TDAUserStruct

- (nonnull instancetype)initWithId:(int32_t)id
                          username:(nonnull NSString *)username
                          password:(nonnull NSString *)password
                              auth:(nonnull NSString *)auth
                          deviceid:(nonnull NSString *)deviceid
{
    if (self = [super init]) {
        _id = id;
        _username = [username copy];
        _password = [password copy];
        _auth = [auth copy];
        _deviceid = [deviceid copy];
    }
    return self;
}

+ (nonnull instancetype)userStructWithId:(int32_t)id
                                username:(nonnull NSString *)username
                                password:(nonnull NSString *)password
                                    auth:(nonnull NSString *)auth
                                deviceid:(nonnull NSString *)deviceid
{
    return [[self alloc] initWithId:id
                           username:username
                           password:password
                               auth:auth
                           deviceid:deviceid];
}

- (NSString *)description
{
    return [NSString stringWithFormat:@"<%@ %p id:%@ username:%@ password:%@ auth:%@ deviceid:%@>", self.class, (void *)self, @(self.id), self.username, self.password, self.auth, self.deviceid];
}

@end
