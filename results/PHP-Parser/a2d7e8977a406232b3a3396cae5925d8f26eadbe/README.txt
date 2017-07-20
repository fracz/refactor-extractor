commit a2d7e8977a406232b3a3396cae5925d8f26eadbe
Author: Nikita Popov <nikita.ppv@googlemail.com>
Date:   Sat Feb 28 18:44:28 2015 +0100

    Use real properties for storing subnodes

    Instead of storing subnodes in a subNodes dictionary, they are
    now stored as simple properties. This requires declarating the
    properties, assigning them in the constructor, overriding
    the getSubNodeNames() method and passing NULL to the first argument
    of the NodeAbstract constructor.

    [Deprecated: It's still possible to use the old mode of operation
    for custom nodes by passing an array of subnodes to the constructor.]

    The only behavior difference this should cause is that getSubNodeNames()
    will always return the original subnode names and skip any additional
    properties that were dynamically added. E.g. this means that the
    "namespacedName" node added by the NameResolver visitor is not treated
    as a subnode, but as a dynamic property instead.

    This change improves performance and memory usage.