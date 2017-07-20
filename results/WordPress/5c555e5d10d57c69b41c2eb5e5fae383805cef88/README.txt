commit 5c555e5d10d57c69b41c2eb5e5fae383805cef88
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Sun Oct 23 18:16:42 2016 +0000

    Accessibility: Improve the Tags meta box accessibility.

    - changes the "X" links in buttons, improves their color contrast ratio and focus style
    - adds screen reader text "Remove item: + tagname"
    - uses `wp.a11y.speak()` to give screen reader users feedback when adding/removing tags
    - makes the `tagcloud-link` toggle a button, with an `aria-expanded` attribute to indicate the tag cloud collapsed/expanded state
    - changes colors for the autocomplete highlighted option in order to have a better color contrast ratio
    - reduces the font size for the autocomplete on Press This
    - removes CSS related to the old `suggest.js` from Press This

    Props joedolson, cgrymala, azaozz, afercia.
    Fixes #27555.

    Built from https://develop.svn.wordpress.org/trunk@38880


    git-svn-id: http://core.svn.wordpress.org/trunk@38823 1a063a9b-81f0-0310-95a4-ce76da25c4cd