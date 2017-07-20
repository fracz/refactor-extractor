commit caa5c0cc761484e634124bf354f3f43c47ad878a
Author: Nikita Popov <nikita.ppv@googlemail.com>
Date:   Sun Oct 9 00:59:44 2016 +0200

    Graceful handling for "special" errors

    Nearly all special errors are now handled gracefully, i.e. the
    parser will be able to continue after encountering them. In some
    cases the associated error range has been improved using the new
    end attribute stack.

    To achieve this the error handling code has been moved out of the
    node constructors and into special methods in the parser.