commit b5538e642118209add5abd7c5da1472a0b608b07
Author: Daniel Sandler <dsandler@android.com>
Date:   Fri Apr 12 15:16:53 2013 -0400

    Rebuild quick settings tile layouts.

    The chief motivation here is to allow two lines of text when
    necessary. So much refactoring over so small a thing, but
    the result is satisfying: most quick settings tiles do not
    need their own layouts. Additionally, tiles with odd-shaped
    icons (I'm looking at you, alien potato mode) no longer fall
    off the grid.

    It should be possible to further reduce the complexity of
    quick settings, but for now this will suffice.

    Bug: 7216734 // vertical alignment issues
    Bug: 7216868 // wrap text in QS tiles
    Bug: 7365911 // NPE in some tiles
    Change-Id: I0c6ef275e44f745dfac52c2a7303072ae48e3873