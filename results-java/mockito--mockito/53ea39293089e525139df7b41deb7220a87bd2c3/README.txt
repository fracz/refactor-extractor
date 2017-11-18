commit 53ea39293089e525139df7b41deb7220a87bd2c3
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Nov 19 13:10:33 2016 -0800

    Refactoring JUnit rule implementation

    Needed for work on Mockito strictness, see #769. Decoupled JUnit API from the work Mockito does before and after every test.

    No public API exposed, listeners and friends need to be refactored and documented.

    Don't merge. This refactoring on its own does not add value.