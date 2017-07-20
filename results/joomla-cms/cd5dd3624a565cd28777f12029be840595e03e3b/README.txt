commit cd5dd3624a565cd28777f12029be840595e03e3b
Author: Brian Teeman <brian@teeman.net>
Date:   Mon May 29 14:28:39 2017 +0100

    [a11y] [com_fields] icons in modals (#15047)

    * [a11y] icons in modals

    Continuing the work to ensure that screen readers dont try to read the value of a font icon.

    This PR could be improved with the use of titles as well if anyone has some suggestions to get the requires strings

    * put tab back

    * Update modal.php

    * changes

    * Update article.xml (#16256)

    * Update article.xml

    * addtional XML CS fixes

    * part 2

    * [a11y] misc admin modules (#15068)

    * logout button on mod_logged

    * multilingual status icon

    * admin stats modules line icons

    * admin status module

    * home icon on default menu

    * update

    I went for the `<span class="element-invisible">` instead of the aria-title so it is more consistent with other uses elsewhere