commit a0026f9cc0c7e5f1dd113177ce3b4d026ebe6985
Author: Thomas Eichstädt-Engelen <thomas@openhab.org>
Date:   Mon Nov 2 10:20:35 2015 +0100

    Updated Lightwave Binding including:
    1) Improved the message sending and receiving so both can happen at the same time
    2) Added support for Relays
    3) Added support for Energy Monitor
    4) Added support for status message from lightwave wifi link
    5) Tidied code and improved inheritence
    6) Added Functional Test
    7) Added All Off Message Support
    8) Added support for Moods
    9) Fixed UPDATETIME on Wifi Link and Heat Info Messages
    10) Fixed issue where an unknown message can cause the receiver thread to crash.
    11) Fix issue when receiving a message from Andriod
    12) Allow items to be set as IN, OUT, or IN_AND_OUT to decide if they update from external messages.