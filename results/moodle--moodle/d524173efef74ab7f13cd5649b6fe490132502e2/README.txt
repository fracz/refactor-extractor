commit d524173efef74ab7f13cd5649b6fe490132502e2
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Mon Jan 4 18:16:02 2010 +0000

    MDL-19932 Rubric grading strategy implemented

    The only weak point here is that we store a raw grade into
    workshop_grades and not a direct id of the selected level. Therefore,
    when re-assessing, we need to actually guess what level the assessor
    chose previously. This is not problem if there are not two levels with
    the same grade. Such case is not common when using Rubric. In the
    future, this may get refactored so Rubric would use its own storage of
    filled assessment forms.