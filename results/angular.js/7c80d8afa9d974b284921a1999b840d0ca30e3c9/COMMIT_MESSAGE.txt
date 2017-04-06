commit 7c80d8afa9d974b284921a1999b840d0ca30e3c9
Author: Georgios Kalpakas <kalpakas.g@gmail.com>
Date:   Fri Oct 7 18:58:53 2016 +0300

    refactor(ngClass): remove redundant `$observe`r and dependency on `$animate`

    Includes the following commits (see #15246 for details):

    - **refactor(ngClass): remove unnecessary dependency on `$animate`**

    - **refactor(ngClass): remove redundant `$observe`r**

      The code was added in b41fe9f in order to support having both `ngClass` and
      interpolation in `class` work together. `ngClass` has changed considerably since
      b41fe9f and for simple cases to work the `$observe`r is no longer necessary (as
      indicated by the expanded test still passing).

      That said, it is a [documented known issue][1] that `ngClass` should not be used
      together with interpolation in `class` and more complicated cases do not work
      anyway.

    [1]: https://docs.angularjs.org/api/ng/directive/ngClass#known-issues