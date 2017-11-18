commit 4d07bc99f014dfa112f635148cc2fbb3d73e983e
Author: Alan Viverette <alanv@google.com>
Date:   Mon Nov 16 10:19:12 2015 -0500

    Clean up usage of temporary TypedValue in Resources

    Reduces the amount of time that a lock is held and reduces the scope of
    the lock to only manage the temporary TypedValue. Also ensures that the
    typed value is consistently returned to the (single item) pool.

    Additionally, performs some refactoring:
    - removes unused variables and constants
    - moves the NotFoundException cause into the constructor for consistency
      with other Exceptions
    - inlines sPreloadedDensity which was not used anywhere
    - fixes line wrapping and indentation in nearby code

    Aside from improvements to locking, there are no functional changes in
    this CL.

    Change-Id: I8c3059261e3cc2288a086e6637ab946e0b7d3741

    Clean up usage of temporary TypedValue in Resources

    Reduces the amount of time that a lock is held and reduces the scope of
    the lock to only manage the temporary TypedValue. Also ensures that the
    typed value is consistently returned to the (single item) pool.

    Additionally, performs some refactoring:
    - removes unused variables and constants
    - moves the NotFoundException cause into the constructor for consistency
      with other Exceptions
    - inlines sPreloadedDensity which was not used anywhere
    - fixes line wrapping and indentation in nearby code

    Aside from improvements to locking, there are no functional changes in
    this CL.

    Change-Id: I8c3059261e3cc2288a086e6637ab946e0b7d3741