commit 25bce3a673afef6a7858270afae4395b4ab53de3
Author: Wei-Ta Chen <weita@google.com>
Date:   Thu Dec 23 16:31:25 2010 -0800

    Do not merge.

    Backport changes related to BitmapRegionDecoder from HoneyComb to
    Gingerbread.

    Bug: 3309014

    ////////////////////////////////////////////////////
    This is a combination of 7 commits.
    Revert "Do not merge."

    This reverts commit f7681f84918c27f6a626681ce37ed2a236c44e82.

    Change-Id: I46fd710600b1649773eaea2d9abc2b21a592f9a6

    Fix a initialization bug in BitmapRegionDecoder.

    Change-Id: I6c1151fd34970a84d4de52d664d9a5dc464892c5

    Fix segfault when tring to throw IOException.

    Change-Id: I530cc4409ba4ca17cec933afad077c5f60ba554f

    Fix 3122139, where previewing an attachment for the second time will
    fail.

    Use AutoFDSeek to mark and restore the position before we read data from
    the descriptor.

    Change-Id: I3d4f012dce486e19b113bc90a98b94031cfa8195

    Add inPreferQualityOverSpeed into BitmapFactory.Options.

    The new field allows a developer to use a more accurate by
    slightly slower IDCT method in JPEG decode. This in turns improves the
    quality of the reconstructed image.

    The field by default is not set and thus does not affect existing
    applications.

    Bug: 3238925
    Change-Id: I93d55b7226e47a43e639325cd1a677694d6f2ee4

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

    Correct the API comments.

    BitmapRegionDecoder supports PNG as well.