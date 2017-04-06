commit ab111b6018242aabb13f2923020416b24ab1ff93
Author: Hardy Ferentschik <hardy@hibernate.org>
Date:   Wed Nov 23 17:24:29 2011 +0100

    HHH-6843 Updating LockTest to be less aggressive and more inline with actual isolation requirements

    The actual changes are in the refactoring of LockTest#testContendedPessimisticLock which got split up into several methods and has the asserts changed

    The formatting changes fix some generics warning and indentation. Used to be two separate commits, but after some git screw-up became one now