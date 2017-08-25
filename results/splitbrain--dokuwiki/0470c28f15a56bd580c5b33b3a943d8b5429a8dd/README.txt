commit 0470c28f15a56bd580c5b33b3a943d8b5429a8dd
Author: Andreas Gohr <andi@splitbrain.org>
Date:   Sat Jan 21 13:48:54 2017 +0100

    refactor Admin UI

    This introduces a new dokuwiki\Ui namespace and refactors the Admin
    screen into a Ui class. The ultimate goal is to split up the big,
    complex functions in inc\html.php in better maintainable classes in the
    Ui namespace. This is the first go at it. Others function->class
    conversions should follow.

    This also switches the icons for our base admin plugins to inline SVG.
    (files and styling not included, yet).