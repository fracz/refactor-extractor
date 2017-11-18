commit cb6216221907d4711fe842c18ee608f50f912924
Author: Jeff Sharkey <jsharkey@android.com>
Date:   Fri Nov 11 14:04:32 2016 -0700

    Make another interface oneway, whitelist provider.

    More progress towards removing blocking calls to improve system
    stability.

    Test: builds, boots
    Bug: 32715088
    Change-Id: I5ab2d2687f4f47e0ee68105c6998e74798af061c