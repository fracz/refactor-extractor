commit 03533715295154f0bb66f84b7c25832d21156805
Author: Geoffrey Pitsch <gpitsch@google.com>
Date:   Thu Jan 5 10:30:07 2017 -0500

    API improvements for creating NotificationChannels

    Remove listener for aysnchronous operation.
    Allow multiple channel creation from a single call.
    Silently no-op for creating existing channels.

    Test: runtest systemui-notification
    Change-Id: Ibc1340d21efa0c12d126bee437adcbb99141e86a