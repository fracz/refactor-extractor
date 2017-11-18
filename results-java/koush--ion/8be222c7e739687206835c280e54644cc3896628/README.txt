commit 8be222c7e739687206835c280e54644cc3896628
Author: Koushik Dutta <koushd@gmail.com>
Date:   Sun Dec 22 16:05:06 2013 -0800

    Loader: minor refactor.

    Add SimpleLoader to do basic no-op implementation.
    Pass in resizeWidth and resizeHeight to the loadBitmap method.
    FileLoader now implements loadBitmap.