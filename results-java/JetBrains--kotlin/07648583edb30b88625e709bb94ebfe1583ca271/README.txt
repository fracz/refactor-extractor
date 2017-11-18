commit 07648583edb30b88625e709bb94ebfe1583ca271
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Thu Dec 19 18:18:39 2013 +0400

    Minor refactoring in callable reference codegen

    Don't store an extra field for functionDescriptor, since it's available from
    the superclass as "callableDescriptor". Store a referenced function descriptor
    instead