commit e37e67eadbd5c8b5342499f8a4f27d644106b3f2
Author: marcin-wosinek <marcin.wosinek@gmail.com>
Date:   Wed Apr 23 11:51:31 2014 +0200

    docs(select): improve naming of `c` variable in example

    It was felt that `c` did not make it clear what the variable held. This
    has been changed to `color` to match the ng-repeat expression above.
    In turn the model value has been changed to `myColor` to prevent a name
    collision.

    Closes #7210