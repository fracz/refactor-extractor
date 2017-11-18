commit f364d23ee96dd4b8bd5123937ebe11524eb66007
Author: dnpetrov <Dmitry.Petrov@jetbrains.com>
Date:   Fri Jun 19 10:51:14 2015 +0300

    KT-5347 VerifyError on local data class with closure

    - generated copy() should push captured fields on stack
    - refactor context lookup
    - handle vars in local classes using instance fields

     #KT-5347 Fixed