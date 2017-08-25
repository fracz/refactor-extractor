commit 225c418fd2d9bc7661a43c292198bab8c5dc209a
Author: Sam Hemelryk <sam@moodle.com>
Date:   Mon Feb 24 10:38:17 2014 +1300

    MDL-41511 blocks: improved how custom block regions were being rendered.

    There is a new theme property 'blockrendermethod' that can be set by the
    theme in its config.php and tells Moodle what method it is using to render
    blocks in the layout files.
    Either blocks, or blocks_for_region.
    Then when adding custom block regions to a page content we ensure we use
    the same method the theme is using elsewhere.

    This is really a hack becuase we (I) didn't properly deprecate
    blocks_for_region when I added the blocks method.