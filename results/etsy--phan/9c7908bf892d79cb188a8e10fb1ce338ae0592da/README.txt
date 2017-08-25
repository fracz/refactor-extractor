commit 9c7908bf892d79cb188a8e10fb1ce338ae0592da
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Thu Jun 22 22:47:38 2017 -0700

    Reduce memory used by UnionType representation.

    Performance improvement:
    - 3.68s before this change
    - 3.60s after this change, with php implementation of runkit_object_id()
    - 3.36s after this change, with native C implementation of runkit_object_id()

    Memory usage decreased by 10%, with or without the native C
    implementation.

    The UnionType implementation details of the Set are rarely important outside
    of a few Phan classes.
    Having SplObjectStorage is inefficient for memory usage - An empty
    SplObjectStorage requires more memory than an empty array,
    and if it's cloned, a clone of the empty SplObjectStorage must be made
    In contrast, creating a clone of an array is easy due to copy on write
    semantics in php (`$x = $y`)

    This uses an array, with the object id as an array key, and the object itself
    as a value.
    - The only pecl I'm aware of that has the desired functionality
      (an integer id for any object, unique for the lifetime of that object)
      is runkit (undocumented, but there for a long time).
      This has a fork in php7 as well.
      This change should work with/without a stripped down module providing
      only the function `runkit_object_id(object $x) : int`
      See https://github.com/runkit7/runkit_object_id

    Fix bug in suppressing PhanRedefineFunctionInternal
    - We check if PhanRedefineFunction was suppressed, but should have
      checked PhanRedefineFunctionInternal

    Add BaseTest, so that static properties can be preserved without being
    touched between test runs. Without the special use properties,
    PHPUnit would modify the private static properties.

    Fix loading the fallback implementation of runkit_object_id, fix test

    The first place the fallback is used is in Type::make(),
    so require_once the fallback in Type.php
    (Need to load fallback when running and in unit tests)

    Also, the Type::make() object caching logic changed, causing object to be
    confused with \\object, etc.
    - Use a different key space for NativeType by adding a unique prefix.
      (Only shows up in the factory method)

    Address review comments.

    Use underscores for variable names.

    Update name in phpdoc

    Use fully qualified function names to speed up calls to those functions.