commit 0c868b08a00138f35eed0de941c062757feaeca3
Author: mudrd8mz <mudrd8mz>
Date:   Fri Sep 11 19:54:11 2009 +0000

    MDL-20268 Significant performance improvement of generate_id()

    Using the PHP's built-in generator instead of the current
    implementation. See the issue description more details.