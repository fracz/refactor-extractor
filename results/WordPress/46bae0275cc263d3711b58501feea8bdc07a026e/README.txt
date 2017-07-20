commit 46bae0275cc263d3711b58501feea8bdc07a026e
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Sun Oct 23 19:57:32 2016 +0000

    Customize: Keep previously uploaded header images in place.

    On the Header Image section, the previously uploaded images disappeared off-screen
    when using the keyboard to navigate and the remove "X" button got keyboard focus.
    Changing the off-screen CSS technique used for the "X" buttons fixes it.

    - improves the focus style on the previously uploaded and suggested images
    - removes a `tabindex="0"` attribute from the current header image

    Fixes #38156.

    Built from https://develop.svn.wordpress.org/trunk@38881


    git-svn-id: http://core.svn.wordpress.org/trunk@38824 1a063a9b-81f0-0310-95a4-ce76da25c4cd