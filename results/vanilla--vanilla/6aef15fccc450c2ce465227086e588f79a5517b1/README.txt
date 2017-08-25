commit 6aef15fccc450c2ce465227086e588f79a5517b1
Author: Robin <robin.jurinka@gmx.net>
Date:   Fri Feb 26 11:30:53 2016 +0100

    Minor coding improvements to class.form.php

    In method date() array for days and years were initialized as empty arrays and right the next step was to add their first element with a static value. I've changed that so that the are initilized with that static element right from the start.

    I've also changed all
    `public function ... (..., $Attributes/$Options = false)` to
    `public function ... (..., $Attributes/$Options = array())`.
    The parameter is expected to be an array, so its default should be an array, too.