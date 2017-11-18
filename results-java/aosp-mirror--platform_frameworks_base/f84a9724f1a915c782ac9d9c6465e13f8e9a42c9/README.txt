commit f84a9724f1a915c782ac9d9c6465e13f8e9a42c9
Author: Clara Bayarri <clarabayarri@google.com>
Date:   Mon Mar 2 16:09:09 2015 +0000

    Move the "Replace" popup option to the Text Selection ActionMode.

    As a first step in unifying the cut/copy/paste ActionMode in Editor
    with the paste/replace popup, I'm moving the replace option to
    the CAB ActionMode. Paste is already there, so for now all options
    are together.

    Missing things to address in upcoming CLs:
    - Invoke the ActionMode in all cases where the popup shows up now,
    ensuring only the options that are currently available show up.
    - Get rid of the current popup
    - Make the ActionMode a floating toolbar (pending feature completion)
    - Define a keyboard shortcut for replace?

    Note that since the ActionMode still shows up in the ActionBar and
    replace has no icon it now appears as text and takes up lots of
    space. This will improve when we can switch to using a floating
    toolbar.

    Change-Id: Ib6b60bae9b58e4db96b9c4cee556e19d3f1bb466