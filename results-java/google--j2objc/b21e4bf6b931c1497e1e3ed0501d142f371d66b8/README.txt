commit b21e4bf6b931c1497e1e3ed0501d142f371d66b8
Author: kstanger <kstanger@google.com>
Date:   Fri Jun 17 06:21:56 2016 -0700

    Replace enclosingName with enclosingClassIdx, using the class name of the enclosing class. Marginally improves binary size and improves performance of getEnclosingClass().

            Change on 2016/06/17 by kstanger <kstanger@google.com>

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=125159590