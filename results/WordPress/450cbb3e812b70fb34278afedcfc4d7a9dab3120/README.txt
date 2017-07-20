commit 450cbb3e812b70fb34278afedcfc4d7a9dab3120
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Fri Jan 22 14:26:27 2016 +0000

    Accessibility: Remove title attributes from the Menus screen.

    Also, adds missing labels and improves the existing ones.
    Updates the "custom links" labels and inputs in the Customizer too.
    Introduces a generic, reusable, `.wp-initial-focus` CSS class to be used for
    the sole purpose of setting the initial focus.
    "Quick Search": uniform the attached events and avoids new AJAX requests to
    be triggered when the pressed key doesn't change the searched term.

    Fixes #35374.
    Built from https://develop.svn.wordpress.org/trunk@36379


    git-svn-id: http://core.svn.wordpress.org/trunk@36346 1a063a9b-81f0-0310-95a4-ce76da25c4cd