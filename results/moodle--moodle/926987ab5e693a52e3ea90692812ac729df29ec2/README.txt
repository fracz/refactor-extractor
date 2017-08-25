commit 926987ab5e693a52e3ea90692812ac729df29ec2
Author: poltawski <poltawski>
Date:   Mon Jul 27 14:18:01 2009 +0000

    lib/simplepie: Improve Integration MDL-7946, MDL-13932

    Added more unit tests, improved style and fixed:
    - Failed behaviour of redirects
    - Incorrect behaviour on feed retrival failure
    - Ensured rss cache clear for unit test setup