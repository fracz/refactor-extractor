commit c6ce85f0a40061cd5c4a6e1b91aa2ace91d9b644
Author: Dmitry Batrak <Dmitry.Batrak@jetbrains.com>
Date:   Wed Oct 14 16:22:09 2015 +0300

    editor new rendering: don't paint outside of clip region

     apart from performance improvement, this also avoids text 'wrapping-around' at 65536 pixels on Linux