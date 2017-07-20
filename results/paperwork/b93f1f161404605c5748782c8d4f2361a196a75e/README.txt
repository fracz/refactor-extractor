commit b93f1f161404605c5748782c8d4f2361a196a75e
Author: Robert K <r@snow87.com>
Date:   Thu Apr 16 20:17:23 2015 -0600

    refactored the retrieve by credentials to only attempt an authentication if we're in a situation where registration might be required.

    Also made it actually return the user object like it's supposed to...