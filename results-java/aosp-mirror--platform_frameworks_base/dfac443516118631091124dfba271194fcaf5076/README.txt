commit dfac443516118631091124dfba271194fcaf5076
Author: Clara Bayarri <clarabayarri@google.com>
Date:   Fri May 15 12:18:24 2015 +0100

    Fix Floating tool bar covers text during long press + drag

    The existing implementation started the ActionMode even when knowing
    a drag would start. Moved this logic to once the drag is complete,
    to avoid the toolbar popping up while the user is still dragging.

    Since the existing method selected the initial word when no other
    selection existed, refactored that logic so it is also performed at
    the start of the drag. Otherwise, the user sees no selection until
    he drags over 2 or more words.

    Bug: 21144634

    Change-Id: I97cf89b1c4c3ebdbbd1af50bd1ce5aa4af72164b