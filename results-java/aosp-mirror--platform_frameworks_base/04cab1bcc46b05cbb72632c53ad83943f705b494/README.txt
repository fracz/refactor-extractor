commit 04cab1bcc46b05cbb72632c53ad83943f705b494
Author: Svetoslav <svetoslavganov@google.com>
Date:   Mon Aug 25 18:35:57 2014 -0700

    Fix accessiblity CTS tests (framework).

    1. An external contribution changed the ordering of views for
       accessibility. While it attempted to fix a platform issue
       for a comparator breaking transitivity, it changed the way
       we order views and results in very unnatural accessibility
       traversal order. It also broke CTS tets. This change tweaks
       the comparator which fixes the tests and improves traversal
       order.

    2. If there is at least one accessibility service which cares
       about windows we register a callback in the window manager
       for window change notifications. We are updating the window
       list on this callback. There was a case where if the service
       requests window updates and immediately asks for the windows
       it gets none as we have not received a callback from the
       window manager yet. Now this call returns after we get the
       callback in a timed fashion. This is consistent with how the
       other introspection APIs work.

    3. Window info objects are cached in the accessibility service
       process. When putting them in the cache a cloning call was
       missing resulting in some cases of clobbering windows given
       to the client. For example, we get some windows, cache them,
       and return these windows to the client. Now a call to clear
       the cache arrives while the user processes the windows and
       the client windows get clobbered.

    4. Added API for checking if a window has accessiblity focus
       to be consistent to the API we have to check whether this
       window has input focus.

    5. Removed some obsolete code.

    bug:16402352

    Change-Id: Ided6da4a82cc0fc703008c58a2dff0119a3ff317