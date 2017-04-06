commit 1a6c9b31438e45180001957bb64f97d1cf54ca4f
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Dec 28 13:22:56 2012 +0100

    [HttpKernel] refactored logging in the exception listener

     * avoid code duplication
     * allow easier overloading of the default behavior