commit 740dd20679b29370e3268248e767549df97554c3
Author: Nicholas K. Dionysopoulos <nicholas@akeebabackup.com>
Date:   Tue Sep 24 17:52:56 2013 +0300

    Improved YubiKey handling

    - Error message shown when you try to activate this feature but the OTP is invalid or doesn't validate against YubiCloud's servers
    - Shuffle the list of YubiCloud servers, using a different, random one every time (helps spread out the load and improve the resilience of the implementation)
    - Use a time based nonce instead of the form token to prevent YubiCloud responding with REPLAYED_OTP if the form token doesn't change between subsequent attempts