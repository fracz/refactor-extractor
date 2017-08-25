commit 3a33fd1e1e997c664f815f34daebb701e744e9d6
Author: Phil Freo <phil@philfreo.com>
Date:   Wed Jan 5 17:32:12 2011 -0800

    Changing references from 'require_once' to 'require' because of performance improvements in relationship to APC.

    APC shouldn't have to open files more than once, but it does if you use require_once instead of require. More details here:
    http://www.techyouruniverse.com/software/php-performance-tip-require-versus-require_once

    It doesn't appear that there's any good reason to use require() in this case.