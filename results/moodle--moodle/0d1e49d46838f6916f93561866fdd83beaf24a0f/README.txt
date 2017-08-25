commit 0d1e49d46838f6916f93561866fdd83beaf24a0f
Author: Aparup Banerjee <aparup@moodle.com>
Date:   Wed Mar 23 17:10:48 2011 +0800

    MDL-26577 added session cleaning to groups_get_activity_group() that fixes an issue with dirty session data. refactored session cleaning into one function, added caching to reduce queries.