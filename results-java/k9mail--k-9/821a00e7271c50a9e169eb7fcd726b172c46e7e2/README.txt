commit 821a00e7271c50a9e169eb7fcd726b172c46e7e2
Author: Fiouz <fiouzy@gmail.com>
Date:   Sat Jun 4 22:57:03 2011 +0200

    MessageList refactoring to remove duplicate code paths.

    Message operations should be more consistent now, regardless of how
    the messages are selected (long click, checkbox+Menu, future group selection).

    This is a backport of the modifications made on the issue258 branch,
    without the threading specific features (no new feature introduced).