commit 092b445ef898e3c1e5b2918b554480940f0f5a28
Author: Jungshik Jang <jayjang@google.com>
Date:   Wed Jun 11 15:19:17 2014 +0900

    Move message handling logic to local device instead of service.

    Local device is in charge of handling incoming messages
    and for some messages such as <set menum language> or
    <report physical address> each device has slightly different
    behavior. Instread of checking destination address and
    local device status from service, this change leaves it
    to each local device.
    Note that some messages are still left on service
    and will be refactored in the following changes.

    Along with this, following changes are included.
    1. add missing jin interfaces
      set_option
      set_audio_return_channel
      is_connected
      Note that get_port_info is under review of jinsuk's change
    2. if tv device receives <Report Physical Address>,
      starts NewDeviceAction.
    3. add constants variables related to new interfaces.
    4. add two helper methods for physical address and vendor
      id handling

    Change-Id: I9c9d04744bc09fbf38431ecfa6e77097b0618a37