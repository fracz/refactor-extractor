commit 4eb3066e8b9c3deca447743a6c6ddcd9ad441872
Author: Brent Shaffer <bshafs@gmail.com>
Date:   Thu Jan 2 17:41:00 2014 -0700

    massive amount of changes to scope handling - 1. Ensures client scopes are always validated 2. ensures client grant types are always validated 3. moves client scope storage to OAuth2\Storage\ClientInterface 4. refactors storage class to interface change 5. TokenController now requires OAuth2\Storage\ClientInterface