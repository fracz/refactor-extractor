commit 8caba22968b5c143b2d650cf9a329f87e89cfb0c
Author: Gerard (Gerry) Caulfield <gerry@moodle.com>
Date:   Wed Dec 14 09:05:52 2011 +0800

    MDL-30724 Limit assignment_count_real_submissions() to only select real
    submissions

    I've also refactored some code so that it is more structured and allows
    for easier overriding of function associated with counting submissions
    and updated associated doc blocks.