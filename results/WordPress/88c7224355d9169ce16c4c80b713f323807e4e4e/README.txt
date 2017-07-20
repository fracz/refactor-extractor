commit 88c7224355d9169ce16c4c80b713f323807e4e4e
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Wed Sep 21 17:54:28 2016 +0000

    Accessibility: Add Themes Feature Filter form improvements.

    - adds "Apply Filters" and "Clear" at the end of the form
    - changes the "Feature Filter", "Apply Filters", "Clear", and "Edit" links in `<button>` elements
    - removes a leftover from [27963]
    - clarifies one button text and adds an `aria-label` attribute
    - adds a `wp.a11y.speak()` message when clicking on "Apply Filters" and no features are selected

    Fixes #38086.

    Built from https://develop.svn.wordpress.org/trunk@38640


    git-svn-id: http://core.svn.wordpress.org/trunk@38583 1a063a9b-81f0-0310-95a4-ce76da25c4cd