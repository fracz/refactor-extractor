commit dacc1509346eaec6113ad47352fa1e2d2464076c
Author: Nik Everett <nik9000@gmail.com>
Date:   Wed Feb 1 21:57:07 2017 -0500

    Expose multi-valued dates to scripts and document painless's date functions (#22875)

    Implemented by wrapping an array of reused `ModuleDateTime`s that
    we grow when needed. The `ModuleDateTime`s are reused when we
    move to the next document.

    Also improves the error message returned when attempting to modify
    the `ScriptdocValues`, removes a couple of allocations, and documents
    that the date functions are available in Painless.

    Relates to #22162