commit e38ab38b1f82f128c0ac7e6130c71ee43ef80495
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Sun Apr 17 15:41:27 2016 +0000

    Accessibility: Customizer, improve UI controls in `customize.php`

    - makes the mobile preview/customize toggle a button
    - changes the "Close" link hidden text from 'Cancel' to 'Close the Customizer and go back to the previous page'
    - adds a missing `type="button"` attribute
    - removes unnecessary `keydown` events and `preventDefault()`: buttons don't need a keydown event and when they have a `type="button"` attribute there's no default action to prevent

    Props Cheffheid, afercia.

    Fixes #31487.
    Built from https://develop.svn.wordpress.org/trunk@37230


    git-svn-id: http://core.svn.wordpress.org/trunk@37196 1a063a9b-81f0-0310-95a4-ce76da25c4cd