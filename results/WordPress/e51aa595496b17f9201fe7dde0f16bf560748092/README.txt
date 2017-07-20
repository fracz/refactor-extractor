commit e51aa595496b17f9201fe7dde0f16bf560748092
Author: westi <westi@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Sat Dec 19 11:30:54 2009 +0000

    Ensure that bulk comment actions do not remove spaces from search terms.
    Also refactors comment bulk action redirect url building to be consistent with other operations.
    Fixes #11471 props nacin.

    git-svn-id: http://svn.automattic.com/wordpress/trunk@12461 1a063a9b-81f0-0310-95a4-ce76da25c4cd