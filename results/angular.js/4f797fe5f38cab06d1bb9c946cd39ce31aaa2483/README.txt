commit 4f797fe5f38cab06d1bb9c946cd39ce31aaa2483
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Sun Mar 4 00:53:02 2012 -0800

    refactor(testabilityPatch): Change JSTD fail to more general throw

    "fail" is a JSTD specific API, so it's not defined when testing without JSTD (eg SlimJim).