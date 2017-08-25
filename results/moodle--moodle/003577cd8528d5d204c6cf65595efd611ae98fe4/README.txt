commit 003577cd8528d5d204c6cf65595efd611ae98fe4
Author: Damyon Wiese <damyon@moodle.com>
Date:   Mon May 4 14:20:30 2015 +0800

    MDL-50085 output: Remove component and subtype from renderer

    They were only added to support templates, and due to refactoring
    are not required any more. So we should remove this API change.