commit a42823ad459eef6d07ea005d9b63074ad79ca56c
Author: Jason Tedor <jason@tedor.me>
Date:   Fri Dec 11 19:03:16 2015 -0500

    Improve ThreadLocal handling in o.e.c.Randomness

    This commit improves the handling of ThreadLocal Random instance
    allocation in o.e.c.Randomness.
     - the seed per instance is no longer fixed
     - a non-dangerous race to create the ThreadLocal instance has been
       removed
     - encapsulated all state into an static nested class for safe and lazy
       instantiation