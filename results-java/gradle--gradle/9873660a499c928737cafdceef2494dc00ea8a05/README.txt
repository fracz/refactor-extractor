commit 9873660a499c928737cafdceef2494dc00ea8a05
Author: Lóránt Pintér <lorant@gradle.com>
Date:   Thu Apr 28 16:43:32 2016 +0200

    Add missing annotation to task input property `Wrapper.gradleVersion`

    Also reorganized existing annotations to always be present on the
    getter instead of the private field.

    +review REVIEW-5932