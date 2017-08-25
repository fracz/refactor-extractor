commit 2240ea1f316156f3cb4475ea23a16246c999b6f0
Author: Andreas Gohr <andi@splitbrain.org>
Date:   Sun Aug 12 12:00:37 2012 +0200

    first start at refactoring the subscription system BROKEN

    This introduces a class for nicer wrapping and easier testing. Some
    functions were changed to provide nicer APIs (no throwing around of
    unescaped regexps) and to simplify things (hopefully).

    The refactoring isn't completed yet, so this will break the subscription
    system.

    The goal is to move as much subscription related stuff to this class as
    possible. Currently there is some code in lib/exe/indexer.php and maybe
    elsewhere (common.php?). Additionally everything should be covered by
    tests. A few tests are included here already.