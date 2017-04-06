commit 35a21532b73d5bd84b4325211c563e6a3e2dde82
Author: Misko Hevery <misko@hevery.com>
Date:   Fri May 1 16:12:53 2015 -0700

    refactor($sanitize): new implementation of the html sanitized parser

    This implementation is based on using inert document parsed by the browser

    Closes #11442
    Closes #11443
    Closes #12524