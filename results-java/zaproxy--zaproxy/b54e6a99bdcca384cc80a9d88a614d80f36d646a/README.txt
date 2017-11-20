commit b54e6a99bdcca384cc80a9d88a614d80f36d646a
Author: lightsey <john@nixnuts.net>
Date:   Fri May 2 03:49:59 2014 +0000

    Issue 1156: The proxy handling of gzip encoded messages was removing newlines
    from the decoded message. This broke minified javascript files where the
    newlines were used to separate statements.

    Also improves the handling of content-length in the modified response by only
    changing this header if it was supplied in the original response and using
    the byte length rather than the string length.