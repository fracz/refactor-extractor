commit 38356e90242dcd74931ef460dc76e6d9f7357c9e
Author: Erik Kline <ek@google.com>
Date:   Mon May 8 17:56:35 2017 +0900

    Refactor and improve logging.

    Test: as follows
        - build
        - flashed
        - booted
        - "runtest frameworks-net" passed
        - "dumpsys connectivity" shows new log output
    Bug: 32163131
    Bug: 36504926

    Merged-In: Ieb9a8e3f85f41d18056a7827997f12986ff13ca1
    Merged-In: I744b048224e0b8bf6fe4da763833b755441c0911
    Merged-In: Ic0c384547687572058d5ef62977163b2560bfc69
    Merged-In: Iae91c43d1bfd9fbedc88821a0bd3a5168ad3b719
    Merged-In: I52606d4375c9b16de03123995737a994ba58b4d7
    Merged-In: I35110b6479280abc650e0ee257045d241923faf9
    Change-Id: I14d6da18660223f7cace156cb6594ee18928a7b0
    (cherry picked from commit 1fdc2e23b5d8136e06cafd7de896b49e5f929c7f)