commit fd1f45a7cf6971e552c2ee0c8e4872ebec4c5b10
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Thu Mar 10 22:37:26 2016 +0000

    Accessibility: Improve accessibility for the Plugin details modal.

    The plugin details modal can be invoked from several screens. There's now a new
    `.open-plugin-details-modal` CSS class to be used in combination with the
    `.thickbox` CSS class that adds everything needed for accessibility.

    - Adds an ARIA role `dialog` and an `aria-label` attribute to the modal
    - Adds a `title` attribute to the iframe inside the modal
    - Constrains tabbing within the modal
    - Restores focus back in a proper place when closing the modal

    Also, improves a bit the native Thickbox implementation: it should probably be
    replaced with some more modern tool but at least keyboard focus should be moved
    inside the modal.

    Fixes #33305.
    Built from https://develop.svn.wordpress.org/trunk@36964


    git-svn-id: http://core.svn.wordpress.org/trunk@36932 1a063a9b-81f0-0310-95a4-ce76da25c4cd