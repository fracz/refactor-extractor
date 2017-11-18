commit 88bf0f91445cfa1fa52a2c45bbfac480dcd238ba
Author: Lari Hotari <lari.hotari@gradle.com>
Date:   Thu Jan 14 12:13:35 2016 -0500

    Refactor change reporting to use FileWatcherEventListener

    - improve efficiency of change reporting
    - use callback approach instead of returning all events that occurred

    +review REVIEW-5788