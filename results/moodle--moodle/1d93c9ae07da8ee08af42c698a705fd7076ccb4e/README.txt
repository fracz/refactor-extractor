commit 1d93c9ae07da8ee08af42c698a705fd7076ccb4e
Author: David Mudrák <david@moodle.com>
Date:   Wed Aug 21 15:13:08 2013 +0200

    MDL-42711 import learnmoodle fixes: Fix the broken links in RSS feed block

    If the <description> of the feed contains URLs longer than 30 characters, the
    URL is split because of the break_up_long_words(). When combined with the
    filter that converts URLs to links, this produces broken links.

    The proper solution would be to improve break_up_long_words() so that it does
    not modifies URLs at all. As a temporary solution for our purpose now is to
    call format_text() prior to break_up_long_words() as it will not modify the
    inner content of the <a> tag.

    This should be fixed upstream. See also discussion at
    https://moodle.org/mod/forum/discuss.php?d=34947