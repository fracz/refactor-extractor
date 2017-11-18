commit 5bdfc8ef4dcd249f379b32ca0f35f41833fdfbd8
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Sat Feb 18 00:00:41 2017 +0300

    Performance improvement: Fix PythonPathCache
    * null means "no data in cache" while empty is valid result means
    no element for this qname. Negative cache was broken. It is fixed now.