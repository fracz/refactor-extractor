commit 17c78cceb05f83032429f07ab0cc3043da6b2adf
Author: Marina Glancy <marina@moodle.com>
Date:   Wed Jun 22 13:10:08 2016 +0800

    MDL-54968 user: change query for resetting dashboard

    In case of inconsistent data the fatal error could have happen.
    Also improve performance by joining several queries into one.