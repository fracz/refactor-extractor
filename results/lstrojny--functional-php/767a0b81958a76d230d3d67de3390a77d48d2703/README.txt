commit 767a0b81958a76d230d3d67de3390a77d48d2703
Author: Adrien Brault <adrien.brault@gmail.com>
Date:   Wed May 13 17:40:37 2015 +0200

    Avoid file_exists during import

    This should improve performance on slow filesystems.