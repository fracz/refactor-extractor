commit 76e9398df94ca5fec77543278817b1b01296b006
Author: iglocska <andras.iklody@gmail.com>
Date:   Sat Dec 31 09:04:46 2016 +0100

    new: Various new feed features

    - import feed descriptor json pastes to add a list of pre-defined feeds
    - improvements to the feed pull (a single non validating attribute shouldn't break the process)
    - altered the saving of the attributes to happen in chunks during a feed pull to avoid very large feeds from stalling the process
    - split the feeds into 3 tabs: default, custom, all