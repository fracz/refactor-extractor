commit dc03c61fe3cd8d0805480e48a974986439977a60
Author: Eric Laurent <elaurent@google.com>
Date:   Fri Apr 1 10:59:41 2011 -0700

    Bluetooth SCO audio API improvements.

    The AudioManager API to control bluetooth SCO did not provide an easy way for
    applications to handle SCO connection errors. When a request to activate SCO with
    AudioManager.startBluetoothSco() failed, no state change was indicated via
    AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED intent. The application had to
    implement a timeout to handle connection failures.

    The API change consists in defining a new intent AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED
    and deprecate AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED. The new intent
    will broacast a new state CONNECTING when the SCO connection is initiated.
    The application can monitor changes from CONNECTING to either CONNECTED or DISCONNECTED
    states to detect connection success or failure.
    An extra indicating the previous state is also added to the new intent.

    Also improved BluetoothHeadset service management in AudioService. A disconnection
    from the service is not considered as a device or SCO link disconnection. Instead, if the
    service interface is not present when a request to activate SCO is received, an
    attempt is made to reconnect to the service.

    Change-Id: I005fda1caaf74bb7de64fece44e9c7e628e828db