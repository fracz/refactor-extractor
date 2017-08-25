commit 182d9990f19f2cde73270c7afb00c62e07c86ea5
Author: David Mudrák <david@moodle.com>
Date:   Thu Nov 19 13:21:49 2015 +0100

    MDL-52214 core: Fix the is_web_crawler() regression

    In MDL-50891, the is_web_crawler() was refactored into a core_useragent
    method and the function itself was deprecated. However, there were no
    unit tests kept to check the backwards compatible behaviour. It turned
    out that the deprecated function leads to PHP fatal error due to a typo.

    This patch fixes the typo and brings back the previous unit tests. To be
    able to explicitly check the raised debugging message, the test case
    now must be subclass of advanced_testcase.

    Additionally fixes missing info about the function being deprecated.