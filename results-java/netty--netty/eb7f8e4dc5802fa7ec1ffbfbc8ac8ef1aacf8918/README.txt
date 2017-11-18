commit eb7f8e4dc5802fa7ec1ffbfbc8ac8ef1aacf8918
Author: Dmitry Spikhalskiy <dmitry@spikhalskiy.com>
Date:   Thu Oct 13 00:56:22 2016 +0300

    Expose RoundRobinInetAddressResolver

    Motivation:
    Make small refactoring for recently merged PR #5867 to make the code more flexible and expose aggressive round robin as a NameResolver too with proper code reuse.

    Modifications:
    Round robin is a method of hostname resolving - so Round robin related code fully moved to RoundRobinInetAddressResolver implements NameResolver<InetAddress>, RoundRobinInetSocketAddressResolver is deleted as a separate class, instance with the same functionality could be created by calling #asAddressResolver.

    Result:
    New forced Round Robin code exposed not only as an AddressResolver but as a NameResolver too, more proper code and semantic reusing of InetNameResolver and InetSocketAddressResolver classes.