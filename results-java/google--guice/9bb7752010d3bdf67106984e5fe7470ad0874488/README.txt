commit 9bb7752010d3bdf67106984e5fe7470ad0874488
Author: lukes <lukes@google.com>
Date:   Thu Jul 21 10:14:54 2016 -0700

    Don't null out the stack entries when popping (or even reference the stack at all), there isn't a point, we just need to adjust the stack height

    This results in a minor runtime improvement

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=128074376