commit dd5bc0d28e89f0000527764f964ba0dcd91acbc4
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Fri Jun 17 20:47:28 2016 +0000

    Accessibility: Theme Installer, make the "Upload Theme" button... a button.

    UI controls that "do something" on a page shouldn't be links. This link behaves
    like a toggle to expand the uploader panel and should be a `button` element with
    an aria-expanded attribute. Also:

    - improves consistency with the Plugin uploader
    - keeps the themes list visible when the uploader is open
    - displays a notice when JavaScript is off
    - adds some `hide-if-no-js` CSS classes
    - removes the `themes.router.navigate()` "upload" route: seems unnecessary and breaks history

    Fixes #35457.
    Built from https://develop.svn.wordpress.org/trunk@37742


    git-svn-id: http://core.svn.wordpress.org/trunk@37707 1a063a9b-81f0-0310-95a4-ce76da25c4cd