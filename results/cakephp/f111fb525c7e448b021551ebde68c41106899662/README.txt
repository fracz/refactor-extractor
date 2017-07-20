commit f111fb525c7e448b021551ebde68c41106899662
Author: AD7six <andydawson76@gmail.com>
Date:   Sun Nov 9 10:07:33 2014 +0000

    account for php's ?>\n handling

    rather than force templates to have, what looks like, extra new lines in
    them, change the render logic to account for the way php ignores a
    newline after a closing php tag by putting two. This is only applied to
    short echo tags, so could be improved upon, and doesn't apply if it's
    the last line of a template.