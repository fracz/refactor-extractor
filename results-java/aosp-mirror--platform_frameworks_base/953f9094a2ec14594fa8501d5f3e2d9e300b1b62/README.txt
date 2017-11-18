commit 953f9094a2ec14594fa8501d5f3e2d9e300b1b62
Author: Wei-Ta Chen <weita@google.com>
Date:   Fri Dec 3 14:06:18 2010 -0800

    Add inPreferQualityOverSpeed into BitmapFactory.Options.

    The new field allows a developer to use a more accurate by
    slightly slower IDCT method in JPEG decode. This in turns improves the
    quality of the reconstructed image.

    The field by default is not set and thus does not affect existing
    applications.

    Bug: 3238925
    Change-Id: I93d55b7226e47a43e639325cd1a677694d6f2ee4