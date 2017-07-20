commit 822b8afb505d1b42663919bc2ef75d5196dded5a
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Fri Mar 31 17:38:43 2017 +0000

    Accessibility: Improve the Media Library inline uploader accessibility.

    For better accessibility, expandable panels should be placed immediately after
    the control that expands them. This change moves the Media Library inline
    uploader up, right after the "Add New" button, also introducing consistency with
    the Plugin and Theme uploaders.
    Adds a proper ARIA role on the button and an `aria-expanded` attribute to give
    better feedback to assistive technologies users about the uploader's expanded state.
    Improves the focus handling when closing the uploader, improves the focus style
    and color contrast ratio of the uploader "close" button.

    Props mantismamita, karmatosed, adamsilverstein, afercia.
    Fixes #37188.

    Built from https://develop.svn.wordpress.org/trunk@40359


    git-svn-id: http://core.svn.wordpress.org/trunk@40266 1a063a9b-81f0-0310-95a4-ce76da25c4cd