commit 3bb92bbe041c4fcb8fa2e6afe0704425d751b867
Author: sam_marshall <sam_marshall>
Date:   Mon May 14 12:11:47 2007 +0000

    MDL-9382: Added fields and orderby parameters to get_user_capability_course, which allows for greatly improved performance in the case where you want more information about the courses. (However, the function does still basically suck.)