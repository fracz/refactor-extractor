commit 13b4dce1cafe20f5a762f88f12b912213351b798
Author: Alexey Kudravtsev <cdr@intellij.com>
Date:   Fri Mar 17 17:57:35 2017 +0300

    do not call canonicalize to improve performance for IDEA-169698 VirtualFIle.findChild() is slow on negative lookups