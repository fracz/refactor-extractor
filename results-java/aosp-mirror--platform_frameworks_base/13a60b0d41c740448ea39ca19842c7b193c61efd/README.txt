commit 13a60b0d41c740448ea39ca19842c7b193c61efd
Author: destradaa <destradaa@google.com>
Date:   Thu Jan 15 18:36:01 2015 -0800

    Fix race condition generating READY and NOT_SUPPORTED statuses.

    The race condition only affects when the client registers for several (all) location listeners.
    And the side efects are benign: only the measurement and navigation message status are incurrectly
    being sent to the application, but there are no crashes or any real data from GPS being
    misscommunicated.
    Also:
    - cache the last reported status to filter sending notifications when no changes have occurred
    - do some cleanup and refactoring in the code changed

    Change-Id: I0692e6b70847dc1ee092d7a05a2c6ba3cd9fa147