// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from client.djinni

#include "client_interface.hpp"
#include <memory>

static_assert(__has_feature(objc_arc), "Djinni requires ARC to be enabled for this file");

@class TDAClientInterface;

namespace djinni_generated {

class ClientInterface
{
public:
    using CppType = std::shared_ptr<::client::ClientInterface>;
    using CppOptType = std::shared_ptr<::client::ClientInterface>;
    using ObjcType = TDAClientInterface*;

    using Boxed = ClientInterface;

    static CppType toCpp(ObjcType objc);
    static ObjcType fromCppOpt(const CppOptType& cpp);
    static ObjcType fromCpp(const CppType& cpp) { return fromCppOpt(cpp); }

private:
    class ObjcProxy;
};

}  // namespace djinni_generated
