commit 616d62dd7b090077fe6d1e981746bc4407161020
Author: Josh Guilfoyle <jasta@devtcg.org>
Date:   Thu Apr 2 18:51:23 2015 -0700

    Bring id pretty printing to <fragment> support as well.

    Previously only View instances in the DOM would format ids using human
    readable names.  This refactors the logic into a common class and
    modifies FragmentDescriptor to support this.