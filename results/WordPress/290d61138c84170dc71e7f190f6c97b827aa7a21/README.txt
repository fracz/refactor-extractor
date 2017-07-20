commit 290d61138c84170dc71e7f190f6c97b827aa7a21
Author: Mark Jaquith <mark@wordpress.org>
Date:   Wed Jul 24 05:52:49 2013 +0000

    Fix some sizing issues with video embeds, and improve video/audio embed shortcode flexibility.

    * `loop`, `autoplay`, and `preload` are now available via the shortcode. Use them non-annoyingly, please!
    * Attributes that pass through the filters are now proper key/value pairs, not an array of `key="value"` strings.
    * `preload` defaults to `metadata` for videos. This fixes the vertical video preview and Safari ogv/webm playback issues.
    * Wrap a div around video embeds to combat a ME.js issue with responsive width=100% themes. Props kovshenin.

    Fixes #24134, #24798.

    git-svn-id: http://core.svn.wordpress.org/trunk@24789 1a063a9b-81f0-0310-95a4-ce76da25c4cd