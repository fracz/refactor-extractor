commit 14e0b9c352f02f16c49bb3994575fea63189c33a
Author: Rouven Weßling <me@rouvenwessling.de>
Date:   Tue May 19 19:42:06 2015 +0200

    refactor(ngCsp): use `document.head`

    The `head` property is available from IE9 onwards.

    Closes #11905