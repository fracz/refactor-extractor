commit e7b54c2c6131a7ab7806bb12c0293057007d0d39
Author: Iglocska <andras.iklody@gmail.com>
Date:   Mon Sep 7 10:06:34 2015 +0200

    Fix to a serious bug with adding attributes via the API and performance fixes

    - due to a bug, setting an attribute ID in the /attributes/add API call can lead to overwriting an existing attribute

    performance improvements:

    - massive improvements to the correlation performance
    - improvements to the attribute validation process