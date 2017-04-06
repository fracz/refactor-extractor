commit 7e47a2d016676f37287203f26689cce1ee1eaa0c
Author: Igor Minar <iiminar@gmail.com>
Date:   Tue Oct 12 05:31:32 2010 +0800

    temparary backaward compatibility patch for Controller.init

    - feedback relies on *Controller.init to be called when a Controller is
    being created. this with previous angular refactoring this is not happening
    in angular any more. To make it easier for feedback to transition, this
    change makes $become call controller's init method if present.

    - call to Controller.init from $route.updateRoute was removed. this was
    left there by accident during the previous refactoring.