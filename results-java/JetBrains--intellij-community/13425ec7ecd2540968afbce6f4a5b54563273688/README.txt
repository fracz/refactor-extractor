commit 13425ec7ecd2540968afbce6f4a5b54563273688
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Wed Apr 20 16:27:29 2016 +0300

    [patch]: refactor ApplyPatch dialog to be able execute as modal one

    * optimize vfs refresh requests;
    * provide modality states to invoke later;
    * optimize FilePresentation;
    * note: please, call small not recursive vfs refresh for one file from edt, otherwise when you call it from polledThread in modal dialog then RefreshQueueImpl->queueSession will ignore your custom modalityState even it was provided and get defaultMS which would be NON_MODAL; as a result invokeLater with fireEvents will never start cause invokeLater with NON_MODAL will waitFor() until dialog closed;
    or use process with modal progress indicator and take modality state from dialog( strongly after show()), otherwise Modality state would be non_modal