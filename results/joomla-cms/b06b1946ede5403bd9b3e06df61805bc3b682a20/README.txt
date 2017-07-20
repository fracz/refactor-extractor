commit b06b1946ede5403bd9b3e06df61805bc3b682a20
Author: Minei3oat <Minei3oat@users.noreply.github.com>
Date:   Sun Apr 2 21:14:26 2017 +0200

    fix problem with accordions using parent option (#12005)

    * fix problem with accordions using parent option

    When using parent option the collapsed class wasn't set on closing
    slides to accordion-toggle.

    * improve code style

    add clean line before if
    add space after comma in function call

    * better performance

    array_filter is only needed if there are elements in the array