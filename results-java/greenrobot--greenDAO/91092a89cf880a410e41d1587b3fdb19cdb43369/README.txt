commit 91092a89cf880a410e41d1587b3fdb19cdb43369
Author: Markus <markus@greenrobot>
Date:   Wed Nov 13 23:11:42 2013 +0100

    big test refactoring: don't use ApplicationTestCase as base class, which can be problematic when creating the application during setUp()