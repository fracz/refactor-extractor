commit 50fdce1a696892b88eb62a8a69a0db3ecc1cce2f
Author: Todd Burry <todd@vanillaforums.com>
Date:   Thu Mar 23 17:00:19 2017 -0400

    Add the event manager to the Gdn_Format class

    This is to support plugins tying in to Gdn_Format to do whatever they
    need to do. The EventManager class’ new fireFilter() method is a good
    fit for formatting stuff.

    Note that Gdn_Format has now been stretched way beyond respectable in
    terms of what a static class should do. We will refactor HTML
    formatting at a later date though :/