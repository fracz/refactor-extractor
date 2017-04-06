commit 670cd9c165815c511c71046c89241446c1bd55d5
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Oct 7 11:10:24 2013 -0700

    revert: refactor($parse): only instantiate lex/parse once

    This reverts commit 281feba4caffd14ffbd6dedfb95ad6415cff8483.

    Since Lexer and Parser objects are stateful it is not safe
    to reuse them for parsing of multiple expressions.

    After recent refactoring into prototypical style, the instantiation
    of these objects is so cheap that it's not a huge win to use
    singletons here.