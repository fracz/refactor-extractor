commit 469e65e37f04fa3aa46e5116b6028cd7113308b3
Author: Hidenari Koshimae <hidenari.koshimae@sonymobile.com>
Date:   Fri Feb 5 18:13:17 2016 +0900

    Increase priority for broadcast intent triggered by HW key

    Add the FLAG_RECEIVER_FOREGROUND flag to the broadcast intent
    triggered by hardware key.
    This prevents the framework from delaying the delivery of the
    intent to its recipients, and improves the response for hardware
    key event under heavy load on the system.

    Bug: 28735973
    Change-Id: Ib7f219845be34794f4c7545927e53cc6c2b504a3