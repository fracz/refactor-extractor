commit bdf8b0112928f3bb8b72627dbd46235dc22dbb73
Author: Eric Anderson <ejona@google.com>
Date:   Tue Aug 30 08:55:04 2016 -0700

    core,protobuf: Add simple argument introspection for methods

    The cast required in protobuf makes me question how much I like
    ReflectableMarshaller, but it seems to be pretty sound and the cast is
    more an artifact of generics than the API.

    Nano and Thrift were purposefully not updated, since getting just the
    class requires making a new message instance. That seems a bit lame. It
    probably is no burden to create an instance to get the class, and it may
    not be too hard to improve the factory to provide class information, but
    didn't want to bother at this point. Especially since nano users are
    unlikely to need the introspection functionality.