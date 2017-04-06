commit 3eb2fbf74533639a376c572d4aad767e80d2c209
Author: Tim Ruffles <timruffles@googlemail.com>
Date:   Sun Oct 6 23:01:03 2013 +0100

    docs($provide): improve examples and explanations

    $provide's example seems awkward. Replace with more real-world example,
    using an injected service, where the service defined has a good reason to
    be a singleton.

    There's quite a lot of confusion around $provide:
    http://stackoverflow.com/search?q=angularjs+service+vs+factory
    Tests for example at: http://jsbin.com/EMabAv/1/edit?js,output