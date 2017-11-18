commit 64f59342d41849bd365cb43fad7505d5e3daa417
Author: Mitsuru Oshima <oshima@google.com>
Date:   Sun Jun 21 00:03:11 2009 -0700

    * new screen resolution support impl.
      * use full window for activities, and shift & clip the content
      * refactored the compatibility code, and introdcued Translator class to handle cooridnate translations.
      * removed a workaround to handle an activity with configChagne=rotation in old implementation.
      * I'll fix background issue on rotation in next CL.

      * removed unnecessary scaling code in SurfaceView, which I forgot to remove when I changed SurfaceView
        not to scale the content.