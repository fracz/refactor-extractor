commit 71d65cb713ebfb85ee19f9f3cd17dd915360fe9b
Author: Olivier Paroz <github@oparoz.com>
Date:   Sat Jun 6 16:21:36 2015 +0200

    Fix max preview, some resizing and caching issues and force preview providers to resize their previews properly
    * introduces a method in OC_Image which doesn't stretch images when trying to make them fit in a box
    * adds the method to all key providers so that they can do their job, as expected by the Preview class
    * improves the caching mechanism of Preview in order to reduce I/O and to avoid filling the available disk space
    * fixes some long standing issues
    * **contains mostly tests**