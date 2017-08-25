commit 4f1ffdecfb0d9b86ece36495c6ad1c0e3c8f6d1e
Author: Sem <sembrestels@riseup.net>
Date:   Wed Feb 19 06:40:48 2014 +0100

    fix(embed): file embedding wasn't working for textareas

    Issue was introduced in 8b358c5 by removing the line that applies the text
    change in textarea.

    The way to extend javascript from the rich text editor was to extend a view
    in js/embed/embed. It has been refactored to the 'embed', 'editor' js hook,
    in order to make js/embed/embed convertible to js file. The view extension
    has been deprecated.

    Text areas and rich text editors manage the insertion diferently. In order
    to insert the content in the correct place, we allow rich text editors to
    return false inside the hook to stop the insert, and apply it just once.

    Fixes #6160