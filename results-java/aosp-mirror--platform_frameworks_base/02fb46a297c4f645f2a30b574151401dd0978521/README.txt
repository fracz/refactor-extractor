commit 02fb46a297c4f645f2a30b574151401dd0978521
Author: Irfan Sheriff <isheriff@google.com>
Date:   Wed Dec 8 11:27:37 2010 -0800

    WPS fixes and refactor

    - Allow IP and proxy set up for WPS
    - Use string for WPS pin to avoid losing leading zeros
    - Add a seperate WPS state machine and WpsConfiguration class

    Change-Id: I87f43fff8bba0ae8ff02e5fc495a8bc628a8c8cf