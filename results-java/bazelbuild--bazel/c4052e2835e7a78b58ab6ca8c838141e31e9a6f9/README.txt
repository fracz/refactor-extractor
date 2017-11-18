commit c4052e2835e7a78b58ab6ca8c838141e31e9a6f9
Author: Manuel Klimek <klimek@google.com>
Date:   Tue May 31 17:06:48 2016 +0000

    Remove DepsContext from CppCompilationContext. This simplifies the
    CppCompilationContext as well as preparing it for further refactorings.

    DepsContext was introduced before NestedSets were available and is superfluous
    in a NestedSet world.

    --
    MOS_MIGRATED_REVID=123653478