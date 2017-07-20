commit bc31046e9c068b587cc0f3210126b345c8e3f8c6
Author: Thomas Steur <thomas.steur@googlemail.com>
Date:   Mon Jun 23 23:11:15 2014 +0200

    added new staticCache to remove a lot of duplicated code and to have a nice interface, by caching the default metrics we reduce the calls to translate from 50k to 2k resulting in good performance improvement...