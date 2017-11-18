commit bf133c65c388f93cd408d0cd9fc0599846f881c2
Author: Jake McGinty <me@jake.su>
Date:   Wed May 6 13:53:55 2015 -0700

    refactor emoji code into package

    1) EmojiTextView and EmojiEditText are used instead of
       using code to emojify text.

    2) Emoji categories' code points are specified in XML

    3) EmojiDrawer itself is a fragment, and its pages are
       also fragments, allowing for better memory
       management.

    Fixes #2938
    Fixes #2936
    Closes #3153

    // FREEBIE