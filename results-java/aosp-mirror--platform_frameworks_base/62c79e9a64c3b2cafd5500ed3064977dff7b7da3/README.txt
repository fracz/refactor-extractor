commit 62c79e9a64c3b2cafd5500ed3064977dff7b7da3
Author: Alan Viverette <alanv@google.com>
Date:   Thu Feb 26 09:47:10 2015 -0800

    Implement landscape layout for time picker dialog

    Adds support overriding default alert dialog panel elements by including
    them in the dialog's custom content view, but no public API (yet!) since
    the panel IDs have never been public. Some minor cleanup and refactoring
    in TimePickerDialog. Removes Holo styles for "clock" and "calendar" style
    pickers since they are new in Material. If the new styles are used against
    Holo they will match Material but with Holo primary/accent colors.

    Also implements themed color state lists to resolve TODOs in both time
    and date pickers.

    Bug: 19431361
    Change-Id: I095fd8d653e02d9e5d20d66611432a08a7a5685e