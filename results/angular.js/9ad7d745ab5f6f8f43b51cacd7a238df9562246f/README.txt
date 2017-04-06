commit 9ad7d745ab5f6f8f43b51cacd7a238df9562246f
Author: Tobias Bosch <tbosch1009@gmail.com>
Date:   Tue Sep 9 10:56:42 2014 -0700

    refactor(ngModel): remove $$invalidModelValue and refactor methods

    - define `ngModelGet` and `ngModelSet` to already use
      the getter/setter semantics, so the rest of the code does
      not need to care about it.
    - remove `ctrl.$$invalidModelValue` to simplify the internal logic