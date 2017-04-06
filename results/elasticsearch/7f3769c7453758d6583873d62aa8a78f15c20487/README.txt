commit 7f3769c7453758d6583873d62aa8a78f15c20487
Author: Jay Modi <jaymode@users.noreply.github.com>
Date:   Wed Feb 8 11:55:50 2017 -0500

    Remove ldjson support and document ndjson for bulk/msearch (#23049)

    This commit removes support for the `application/x-ldjson` Content-Type header as this was only used in the first draft
    of the spec and had very little uptake. Additionally, the docs for bulk and msearch have been updated to specifically
    call out ndjson and mention that the newline character may be preceded by a carriage return.

    Finally, the bulk request handling of the carriage return has been improved to remove this character from the source.

    Closes #23025