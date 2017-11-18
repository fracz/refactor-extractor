commit 43ea54bdc343a913f62885304796e4ab1bca4ef1
Author: Jeff Brown <jeffbrown@google.com>
Date:   Fri Mar 9 14:37:48 2012 -0800

    Don't remove all animation callbacks if action/who was null.

    Fixes a bug in View.removeCallbacks and View.unscheduleDrawable
    where it was possible for the caller to remove all animation
    callbacks if it happened to specify an action or who parameter
    of null.

    Also refactored the callback queueing code in Choreographer
    to make it more intent revealing although the behavior remains
    the same.

    Bug: 6144688
    Change-Id: Iba29dcda6b3aaad73af664bb63feab65ae3483e5