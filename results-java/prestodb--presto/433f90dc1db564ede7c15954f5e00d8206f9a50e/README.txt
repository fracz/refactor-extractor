commit 433f90dc1db564ede7c15954f5e00d8206f9a50e
Author: Andrzej Fiedukowicz <Andrzej.Fiedukowicz@teradata.com>
Date:   Fri May 20 14:32:12 2016 +0200

    Fix ReflectionParametricScalar to cover all scalar cases

    This moves parsing all types of scalar functions and operators
    to ReflectionParametric scalar.

    The operation is done as simple as possible as this is only
    first step for further refactoring.

    The main things that are covered in new version of
    ReflectionParametricScalar that were not covered before are:
     - Support for aliases
     - Support for functions only providing exact implementations
     - Support for name generation from methods names