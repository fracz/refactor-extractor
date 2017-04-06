commit faf0c3e4c1b6f79b00f92f1231491f7569ecd25e
Author: BobChao87 <bobchao87@gmail.com>
Date:   Sun Oct 9 11:07:08 2016 -0700

    refactor(ngModelSpec): use valueFn over curry

    Refactor ngModelSpec to use internal helper function `valueFn`.
    Use instead of multiple-defining a function called `curry`.

    PR (#15231)

    Addresses a quick change mentioned in PR 15208 from Issue #14734