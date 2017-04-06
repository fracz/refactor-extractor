commit 18263f1c527f656c0b7194d128b2de7ed2d90641
Author: Martin Staffa <mjstaffa@gmail.com>
Date:   Wed Oct 19 11:55:50 2016 +0200

    chore(docs-app): improve layout when loading partials

    By setting the current partial content to hidden, the current height
    of the window is maintained until the new content is loaded.
    This prevents flickering caused by the scrollbar (dis)appearing and
    the footer coming into view.