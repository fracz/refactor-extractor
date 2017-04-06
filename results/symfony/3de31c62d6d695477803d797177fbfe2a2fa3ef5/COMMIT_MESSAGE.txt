commit 3de31c62d6d695477803d797177fbfe2a2fa3ef5
Merge: d9959af 9c8a283
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Feb 27 10:09:42 2012 +0100

    merged branch snc/session-handler (PR #3436)

    Commits
    -------

    9c8a283 Some \SessionHandlerInterface related documentation updates
    9b2de81 Fixed \SessionHandlerInterface in DbalSessionStorage

    Discussion
    ----------

    Some \SessionHandlerInterface related updates

    ---------------------------------------------------------------------------

    by snc at 2012-02-23T20:01:51Z

    I checked the `Locale` stub in the documentation and it looks like the `\` is not prefixed, so I'll change this, too.

    ---------------------------------------------------------------------------

    by drak at 2012-02-24T07:40:39Z

    We really need some tests for the bridge classes, even if they stubs which cause the compiler to at least parse the class, would pick up refactorings like this.