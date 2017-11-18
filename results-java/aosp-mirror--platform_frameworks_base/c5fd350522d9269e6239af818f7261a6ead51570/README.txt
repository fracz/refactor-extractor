commit c5fd350522d9269e6239af818f7261a6ead51570
Author: Winson <winsonc@google.com>
Date:   Mon Jan 18 15:18:37 2016 -0800

    Removing extraneous calls.

    - Removing calls to ensureCapacity, which was causing reallocations when
      using a temporary set with multiple uses.
    - Fixing issue with update callback not being called when immediately
      updating a TaskView’s transform
    - Adding utility methods in preparation for refactoring code


    Change-Id: If62c3751ed6af15092a34435df08bb4d627536ea