commit b37f181c9853bd0d55d360db95d3bf1bba0b5a6f
Author: Pavlin Radoslavov <pavlin@google.com>
Date:   Wed Jan 25 16:54:07 2017 -0800

    Update the A2DP Codec Config API

    Previously, the JNI upcall would contain only the current codec config.
    In the new API, the upcall contains:
     1. The current codec config
     2. The list of codecs containing the local codecs capabilities
     3. The list of codecs containing the selectable codecs capabilities.
        This list is the intersection of the local codecs capabilities
        and the capabilities of the paired device.

    Also, refactored the Java internals to accomodate the extra information:
     * Added new class BluetoothCodecStatus that contains the extra info:
       current codec config, local codecs capabilities and selectable
       codecs capabilities
     * Renamed method getCodecConfig() to getCodecStatus() and return the
       corresponding BluetoothCodecStatus object.
     * Updates to class BluetoothCodecConfig:
       new methods isValid(), getCodecName(), and updated toString()
       so it is more user friendly
     * Removed BluetoothCodecConfig.EXTRA_CODEC_CONFIG and
       EXTRA_PREVIOUS_CODEC_CONFIG.
       The former is superseded by BluetoothCodecStatus.EXTRA_CODEC_STATUS;
       the latter is not really used.

    Test: A2DP streaming with headsets and switching the codecs
    Change-Id: Ia1af2c22e521e863e28a360610aca49f7e62d31b