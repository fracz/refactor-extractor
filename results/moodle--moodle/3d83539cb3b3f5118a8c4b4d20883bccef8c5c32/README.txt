commit 3d83539cb3b3f5118a8c4b4d20883bccef8c5c32
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Sat Jun 5 19:53:40 2010 +0000

    MDL-22062 Make idnumber of additional module grade items editable in the gradebook

    This patch allows grade items with itemnumber > 0 being edited in the
    gradebook. It also improves the grade_verify_idnumber() so that it does
    not allow the item with itemnumber > 0 have the same idnumber as the
    major grade item with itemnumber 0.