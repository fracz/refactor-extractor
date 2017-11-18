commit 520ea0673e1dd44fc0cac04ab1d0ded5b8a67a52
Author: Jason Monk <jmonk@google.com>
Date:   Tue Aug 18 14:53:06 2015 -0400

    Some QS refactoring

    Get the bit about tiles out into its own place.  This will make it
    easier to experiment with new layouts, and to hide/show other elements
    as necessary without lots of layout work.

    Change-Id: I32752df1881e4e3c693730284f8608102abdc04b