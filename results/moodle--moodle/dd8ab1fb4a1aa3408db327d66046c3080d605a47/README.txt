commit dd8ab1fb4a1aa3408db327d66046c3080d605a47
Author: David Mudrak <david@moodle.com>
Date:   Fri Feb 11 23:20:16 2011 +0100

    MDL-26353 Database activity module: display help icon with the new help strings

    This commit makes the new strings added by Helen used at the Import
    entries page. Note that the syntax of the strings had to be fixed as
    there must not be leading whitespace before the asterisk in Markdown format.

    Also as requested by Helen, the wording or export/import links is
    improved now.

    As a bonus for free, the patch fixes incorrect API usage of get_record()
    and friends.