commit 041a0baba7f075ab3aba9c075dd75695a51617e4
Author: Adam Cohen <adamcohen@google.com>
Date:   Wed Nov 9 20:10:27 2011 -0800

    Deferring wallpaper update to improve workspace scrolling (issue 5506959)

    -> On the Xoom, this change gets us back up to 60 fps. The
       change is really more of a workaround for the fact that we don't
       have vsync, and we ought to be able to change it back once we do.

    Change-Id: I80888f18887bf5f2fed72c19641ed430ef6dbfcf