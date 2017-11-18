commit 23dd3570903879d44c93975c2d49843ddd42a402
Author: Luke Daley <ld@ldaley.com>
Date:   Wed Oct 5 13:07:10 2011 +0100

    added some modelling of our tight completion semantics as EntryPoint (and friends).

    Packaging this into a reusable structure makes things a bit more explicit, and allows us to use the same logic for the daemon (and maybe other gradle processes) without duplication. This will also help to minimise the differences between a background and foreground daemon which will improve the accuracy of our tests.