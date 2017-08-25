commit 637a60dc5fde253587f53b097bfd45652dadec53
Author: Eloy Lafuente <stronk7@moodle.org>
Date:   Tue May 4 14:19:00 2010 +0000

    NOBUG mimetypes - improve a bit mimeinfo_from_icon() so, before using the
    "return last" strategy (leading to all images returning image/tiff always)
    it will try one "return by extension match" if possible.