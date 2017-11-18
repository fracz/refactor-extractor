commit a02965e21199fccf897d6746149a3432f865ce4a
Author: Andrei Stingaceanu <stg@google.com>
Date:   Fri Apr 8 16:42:02 2016 +0100

    Keyboard shortcuts: UI polish 1

    * title color for system groups is now: "material_deep_teal_500"

    * background behind shortcut keys:
    ** Now has 2dp rounded corners
    ** changed color from "material_grey_200" to "material_grey_100"

    * the text items now have a minimum width equal to their height.
    This means that now the text items with one character are always
    the same (square) size as the icon items. Makes the UI look much
    cleaner thus easier to read

    * the line item now has a minimum height of 48dp and the content
    is vertically centered

    * minor variable renaming for increased readability

    Bug: 28075364
    Change-Id: Id7090b607b9c604c55513e7c393ed1084a1c8df0