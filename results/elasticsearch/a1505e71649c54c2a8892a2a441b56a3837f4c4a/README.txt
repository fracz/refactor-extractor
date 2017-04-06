commit a1505e71649c54c2a8892a2a441b56a3837f4c4a
Author: Adrien Grand <jpountz@gmail.com>
Date:   Thu Feb 5 14:42:35 2015 +0100

    Tests: Assert that we do not leak SearchContexts.

    Even if there is a background thread that periodically closes search contexts
    that seem unused (every minute by default), it is important to close search
    contexts as soon as possible in order to not keep unnecessary open files or
    to prevent segments from being deleted.

    This check would help ensure that refactorings of the SearchContext management
    like #9296 are correct.