commit 3892bcb0549a7637247c7923802088b23240a696
Author: Johan Janssens <johan@nooku.org>
Date:   Mon Oct 23 19:56:02 2006 +0000

    Overhaulted site template implementation.
    Removed patTemplate engine to improve speed and flexibility
    Removed jdoc::exist ... />, use countModules function instead
    Removed <jodc::empty ... />, use countModules function instead
    <jdoc::inlcude /> attributes are now also passed to the module chrome functions

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@5558 6f6e1ebd-4c2b-0410-823f-f34bde69bce9