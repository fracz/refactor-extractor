commit 40d7f105ad0e409cfb89bb308825a5d7911fc95f
Author: Martin Traverso <martint@fb.com>
Date:   Mon Mar 18 22:47:57 2013 -0700

    Fix potential NPE and improve clarity

    There was a potential NPE due to the call to "having.orNull()" if the HAVING clause is missing