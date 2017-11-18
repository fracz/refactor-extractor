commit ab6f2f62fec5c6814975cd61a17df06857ac91de
Author: Charles He <qiurui@google.com>
Date:   Wed Jul 12 15:30:00 2017 +0100

    Add alert dialog when always-on VPN disconnects.

    As part of the improvement to always-on VPN, we're adding this dialog
    which is shown when the user taps the "Always-on VPN disconnected"
    notification. This dialog shows a relatively detailed explanation of the
    situation and offers two actions: 1) to attempt to reconnect, and 2) to
    open the VpnSettings page in Settings. As a result, we expect the users
    to be more aware of the consequences of a disconnected VPN, and offer
    them more actionable options.

    Bug: 36650087
    Bug: 65439160
    Test: manual

    Change-Id: I5ae3ff5d25740ea52357012b75d7eb1776dfdc5e
    Merged-In: I5ae3ff5d25740ea52357012b75d7eb1776dfdc5e
    (cherry picked from commit 7376f6c16873e4c8f7c3f7fa27d4be6ea7894014)