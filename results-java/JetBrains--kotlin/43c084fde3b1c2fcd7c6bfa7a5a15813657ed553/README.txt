commit 43c084fde3b1c2fcd7c6bfa7a5a15813657ed553
Author: Alexey Andreev <Alexey.Andreev@jetbrains.com>
Date:   Tue Apr 11 16:01:27 2017 +0300

    Simplify coroutine generation in JS backend
    Stop making aliasing suspend function descriptor with reference to
    instance of state machine. This may cause problems in some cases,
    for example, when compiling recursive suspend function. See KT-17281.
    Instead, make alias for synthetic continuation parameter. This
    additionally required some refactoring, e.g. *always* generating
    continuation parameter during codegen.