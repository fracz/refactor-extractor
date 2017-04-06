commit 4ab268bab2cfd7fc3cb4c4808f706d5049c1fae5
Author: javanna <cavannaluca@gmail.com>
Date:   Wed Sep 10 15:52:47 2014 +0200

    Internal: refactor copy headers mechanism to not require a client factory

    With #7594 we replaced the static `BaseRestHandler#addUsefulHeaders` by introducing the `RestClientFactory` that can be injected and used to register the relevant headers. To simplify things, we can now register relevant headers through the `RestController` and remove the `RestClientFactory` that was just introduced.

    Closes #7675