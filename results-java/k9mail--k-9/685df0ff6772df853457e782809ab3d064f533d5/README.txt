commit 685df0ff6772df853457e782809ab3d064f533d5
Author: Jan Berkel <jan.berkel@gmail.com>
Date:   Sat Feb 19 19:59:38 2011 +0100

    Perf improvement: use parcels instead of serializable

      Standard Java serialization is slow on Android. Replacing it w/
      parcelable makes it around 10x faster (on a N1, with ~ 500 messages
      in the list).

      To avoid further confusion and potential bugs MessageReference was
      made no longer implement Serializable.