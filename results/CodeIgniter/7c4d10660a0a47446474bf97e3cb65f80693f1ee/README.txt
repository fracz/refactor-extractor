commit 7c4d10660a0a47446474bf97e3cb65f80693f1ee
Author: Andrey Andreev <narf@bofh.bg>
Date:   Thu Nov 1 15:14:34 2012 +0200

    Fix issue #1953 (form values being escaped twice)

    Re-instaing an improved form_prep() function, reverting most of the changes from 74ffd17ab06327ca62ddfe28a186cae7ba6bd459.