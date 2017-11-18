commit 634acb9712f2627acf9279a78d120ea2da1e0464
Author: Yao Chen <yaochen@google.com>
Date:   Wed Apr 13 16:17:47 2016 -0700

    Add CarVolumeDialogController in SystemUI for Android Auto.

    Cars usually have an external audio module. When Android is serving as
    the car's headunit, users should be able to adjust the car's volume
    through SystemUI. The following changes are made to make it work:

    + Load VolumeDialogController from SystemUIFactory
    + Added CarSystemUIFactory
    + Added CarVolumeDialogController which extends VolumeDialogController
      and it uses CarAudioManager as source of truth for volume controls.
    + Some refactor in VolumeDialogController to make it easier for
    subclasses to override volume controls.

    Note that CarAudioManager does not completely replace AudioManager.
    Majority of code in VolumeDialogController still applies in the car use
    case, so I made CarVolumeDialogController a subclass of
    VolumeDialogController instead of making them peers.

    Bug: 27595951

    Change-Id: Id4adec7281e41aa71f3de034e5b88a32a89be305