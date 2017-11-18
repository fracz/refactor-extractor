commit f7705c3ed6ac2e6d787f91c65feb003a8b749ceb
Author: xinhe <xinhe@google.com>
Date:   Tue Dec 1 14:44:37 2015 -0800

    Initial codes for Quality network selection

        In this change list, the old WifiAutojoin module is
        refactored initially. The old WifiAutojoinController is
        replaced with a new WifiQualifiedNetworkSelector.
        WifiConfiguration, WifiConfigureStore and
        WifiStateMachine have been modified accordingly. The new
        network selection logic is refactored with a more clear
        and deterministci one. To refer to thedescribed in
        details, in "Quality Network Selection and Connectivity
        Scan Management in N". The link of the document is:
        https://docs.google.com/document/d
        /1JPTa2NEk-PgjCJ16fIrR6ohV-kDKveDlYDOeiMCB2_c

    cherry-picked from 8d106780b6a638552749e54e169fc72537d4bccc
    and make changes on WiFiConfigurationSerializer.java since
    it is not on mm-wireless-dev
    Bug:26012244
    Change-Id: I44e454544b630b891c9a58a18b5a028edcce580f