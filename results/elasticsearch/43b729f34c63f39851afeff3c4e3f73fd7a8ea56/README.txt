commit 43b729f34c63f39851afeff3c4e3f73fd7a8ea56
Author: javanna <cavannaluca@gmail.com>
Date:   Sat Oct 31 12:41:35 2015 +0100

    [TEST] test query and search source parsing from different XContent types

    We used to test only json parsing as we relied on QueryBuilder#toString which uses the json format. This commit makes sure that we now output the randomly generated queries using a random format, and that we are always able to parse them correctly.

    This revealed a couple of issues with binary objects that haven't been migrated yet to be structured Writeable objects. We used to keep them in the format they were sent while parsing, which led to problems when printing them out as we expected them to always be in json format. Also we can't compare different BytesReference objects that hold the same content but in different formats (unless we want to parse them as part of equal and hashcode, doesn't seem like a good idea) and verify that we have parsed the right objects if they can be different formats. The fix is to always keep binary objects in json format. Best fix would be not to have binary objects, which we'll get to once we are done with the search refactoring.

    Closes #14415