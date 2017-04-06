commit 462eefc1e45236bf451fce851ec58169915b71cd
Author: Richard Littauer <richard.littauer@gmail.com>
Date:   Sun May 25 10:38:55 2014 -0700

    docs(ngController): add more description of `controller as` syntax

    Using `controller as` in the template is not described well
    in the docs, as both `scope` injection and `this` are presented
    equally without too much discussion of the advantages of using
    either. I added a bit more discussion based on google's internal
    style guidelines.

    Closes #7591
    Closes #5076 (until Angular 2.0 comes out and we refactor everything)