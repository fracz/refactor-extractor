commit 3a0a2d05635920b64626448302afb12c22bb6cf6
Author: Andreas Gohr <andi@splitbrain.org>
Date:   Sat Jan 22 21:52:30 2011 +0100

    refactored passowrd hashing functions to a class

    this splits the long auth_cryptPassword() function into many member
    functions of a new class PassHash which should make it more
    maintainable and reusable for other projects.

    This also adds two new methods djangomd5 and djangosha1 as used by the
    popular python framework Django.

    Maybe the auth_cryptPassword() and auth_verifyPassword() functions
    should be deprecated in favor of using the class directly?