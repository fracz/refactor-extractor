commit bf687936c054f3655e4d0b8bada125cb14386f1d
Author: dimvar <dimvar@google.com>
Date:   Wed Oct 14 13:20:36 2015 -0700

    [NTI] Handle function types with complex nominal and receiver types.

    Mostly a refactoring. New functionality:
    - Warn when the nominal/receiver type annotation doesn't make sense
    - Slight change in FunctionType#isSubtypeOf for receiver types
    - Bugfix for qmarkfunctions
    - Catching some new warnings in the existing unit tests by knowing the more precise type

    In a follow-up CL, I'll make @this override the receiver type of the prototype.
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=105441709