commit 0a16cf21021bc85cccd743da84897d3abcbabfbe
Author: Christian Flothmann <christian.flothmann@xabbuh.de>
Date:   Wed Sep 3 22:34:28 2014 +0200

    improve handling router script paths

    The `server:run` command switches the working directory before
    starting the built-in web server. Therefore, the path to a custom
    router script had to be specified based on the document root path
    and not based on the user's working directory.