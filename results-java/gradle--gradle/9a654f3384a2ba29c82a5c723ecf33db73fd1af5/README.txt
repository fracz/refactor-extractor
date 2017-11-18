commit 9a654f3384a2ba29c82a5c723ecf33db73fd1af5
Author: Stefan Oehme <stefan@gradle.com>
Date:   Sat Apr 8 23:18:00 2017 +0200

    Remove array reuse

    After several rounds of mesuring, this did not
    show any actualy performance improvement. Removing
    it as it added complexity and was error prone.