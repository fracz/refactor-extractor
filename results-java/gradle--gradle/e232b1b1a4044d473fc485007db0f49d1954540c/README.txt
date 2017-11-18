commit e232b1b1a4044d473fc485007db0f49d1954540c
Author: Daz DeBoer <daz@gradle.com>
Date:   Mon May 2 18:28:51 2016 -0600

    Fix failure to generate a method descriptor for an overloaded method

    For now, we simply don't cache the descriptors for overloaded methods.
    It may be preferable to instead cache based on some key generated
    from the method name and parameter types. Or perhaps even a single weak
    hash map keyed on class/method/params.

    I'm leaving further improvements for @melix to verify using a profiler
    before proceeding.