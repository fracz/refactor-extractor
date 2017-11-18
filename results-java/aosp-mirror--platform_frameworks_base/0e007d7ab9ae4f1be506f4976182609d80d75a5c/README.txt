commit 0e007d7ab9ae4f1be506f4976182609d80d75a5c
Author: Wei-Ta Chen <weita@google.com>
Date:   Mon Dec 6 15:44:18 2010 -0800

    Unhide inPreferQualityOverSpeed in BitmapFactory.Options.

    The new field allows a developer to use a more accurate by
    slightly slower IDCT method in JPEG decode. This in turns improves the
    quality of the reconstructed image.

    The field by default is not set and thus does not affect existing
    applications.

    Bug: 3238925

    Related changes: https://android-git.corp.google.com/g/#change,83291 and
                     https://android-git.corp.google.com/g/#change,83294

    Change-Id: I969f5c413f9b2179454aeb90e18ae8222ee583b4