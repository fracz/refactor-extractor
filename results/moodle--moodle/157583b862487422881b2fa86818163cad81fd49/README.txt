commit 157583b862487422881b2fa86818163cad81fd49
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Aug 2 17:21:32 2012 +0100

    MDL-34704 couse, accesslib: improve $CFG->courselistshortnames

    1. get_context_name should respect the $CFG->courselistshortnames
    setting.

    2. When $CFG->courselistshortnames is on, what to display should use a
    language string, rather than string concatenation. This makes it
    possible for people to configure the display. For example, they might
    want 'My first course [M101]' instead of 'M101 My first course'.