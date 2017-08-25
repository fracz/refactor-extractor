commit 0d9805dc142e0ef2cdc4e2da3a93c07496922f0d
Author: Damyon Wiese <damyon@moodle.com>
Date:   Thu Jun 26 12:15:37 2014 +0800

    MDL-46147 modinfo: performance improvement for course page

    This is a big win in a specific situation, ie filters enabled for content+headings,
    and no activity descriptions visible on the course page.