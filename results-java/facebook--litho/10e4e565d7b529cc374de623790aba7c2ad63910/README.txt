commit 10e4e565d7b529cc374de623790aba7c2ad63910
Author: Ian Childs <ianc@fb.com>
Date:   Mon Jul 31 05:32:02 2017 -0700

    Give Component.Builder its own type

    Summary:
    Going to use this in the next diff to move `key` to the Component.Builder. For now this is just a simple refactor.

    Note: this is a potentially breaking change.

    Reviewed By: passy

    Differential Revision: D5516458

    fbshipit-source-id: f6b702d1ff73dc3dd1148cfc7ba1077dbe01863a