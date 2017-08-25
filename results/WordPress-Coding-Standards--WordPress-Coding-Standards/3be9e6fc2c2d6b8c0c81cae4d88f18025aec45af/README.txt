commit 3be9e6fc2c2d6b8c0c81cae4d88f18025aec45af
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Tue Jan 24 15:20:58 2017 +0100

    Abstracts: Fix potential bug - methods should allow for optionally returning a stack pointer.

    PHPCS allows for the `process()` method to optionally return a stack pointer. If so, it will not call the sniff for the current file again until the returned stack pointer has been reached.

    I did not take that into account in the refactor of the AbstractFunctionRestrictionsSniff and the creation of the AbstractFunctionParameterSniff which basically blocks any child classes from using the ability to return a stack pointer.

    This has now been fixed.