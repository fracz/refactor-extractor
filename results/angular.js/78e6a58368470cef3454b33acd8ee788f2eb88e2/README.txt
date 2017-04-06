commit 78e6a58368470cef3454b33acd8ee788f2eb88e2
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Sun Jun 5 18:30:13 2016 -0700

    refactor($q): remove unnecessary checks/helpers/wrappers

    - Remove internal `makePromise()` helper.
    - Remove unnecessary wrapper functions.
    - Remove unnecessary check for promises resolving multiple times.
      (By following the Promises/A+ spec, we know this will never happen.)
    - Switch from function expressions to (named) function declarations.

    Closes #15065