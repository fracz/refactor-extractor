commit 6bf250f84e457e87f87f05e6f7dd76303a3a5ea5
Author: Damyon Wiese <damyon@moodle.com>
Date:   Thu Oct 29 16:32:25 2015 +0800

    MDL-51961 cbe: UX improvements to manage competencies

    1. The competencies tree should open with all nodes initially collapsed
        (unless there is less than 20 results, in which case they should all be expanded)
    2. When the tree is refreshed, all previously selected nodes should be remembered
        and the tree expanded to that point.
    3. While the tree is reloading - there should be a "spinner"