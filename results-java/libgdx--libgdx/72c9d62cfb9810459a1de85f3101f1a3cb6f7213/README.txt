commit 72c9d62cfb9810459a1de85f3101f1a3cb6f7213
Author: NathanSweet <nathan.sweet@gmail.com>
Date:   Thu Oct 9 14:24:19 2014 +0200

    Various JSON improvements.

    Fixed enums using toString(), should use name().
    Fixed serialization of a Collection other than the known type (wraps with object, similar to a subclassed enum).
    Fixed deserialization of ArrayMap, Collection and friends.
    #2439