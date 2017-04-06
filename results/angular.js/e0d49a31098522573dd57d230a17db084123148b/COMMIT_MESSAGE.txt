commit e0d49a31098522573dd57d230a17db084123148b
Author: Caitlin Potter <caitpotter88@gmail.com>
Date:   Tue Sep 2 02:33:27 2014 -0400

    refactor(ngRepeat): specify explicit `false` for cloneNode deepClone parameter

    This should provide a slight compat improvement for old versions of Opera, which did not treat the
    `false` as the default value.

    There is no test for this fix as Opera 11 is not a browser which runs on the CI servers.

    Closes #8883
    Closes #8885