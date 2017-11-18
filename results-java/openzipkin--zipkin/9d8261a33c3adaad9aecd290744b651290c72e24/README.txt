commit 9d8261a33c3adaad9aecd290744b651290c72e24
Author: Adrian Cole <acole@pivotal.io>
Date:   Wed Feb 3 15:51:51 2016 +0100

    Re-organizes Node so that it can be used on objects besides Span

    We need to create the trace tree in dependency store code. Particularly,
    we don't want to retrieve the entire span just to count calls. This
    refactors the Node object so that it can be used when all span details
    aren't present.