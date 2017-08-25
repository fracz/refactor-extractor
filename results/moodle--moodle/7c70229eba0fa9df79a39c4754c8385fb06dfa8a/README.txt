commit 7c70229eba0fa9df79a39c4754c8385fb06dfa8a
Author: David Mudrak <david.mudrak@gmail.com>
Date:   Mon Jul 12 15:03:42 2010 +0000

    MDL-21137 MNet: UI to choose peer theme replaced with simple selector

    There is no need to have whole featured theme selector with previews
    etc here. Following the same form element as they have in Mahara, the
    theme can now be selected for a given peer at 'Review host details' tab.
    I have also noticed that force_theme and theme columns are redundant at
    the moment. Keeping them both for future improvements, though.