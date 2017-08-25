commit 22efa4f4c15718aa080ab754286ffdefbee338b2
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Fri Jun 14 14:02:25 2013 +0200

    #844: Continue work on the Builder refactoring

    - Moved the Descriptor initialization to a ServiceProvider
    - Added FileDescriptor
    - Implemented all functionality from Reflector Builder into Assemblers
    - Refactored Parser to use new builder.