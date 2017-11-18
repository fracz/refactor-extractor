commit 35af9d2fa2ce4e09b0ca4a0e3a2d36fdcd5a83db
Author: dgitelson <dgitelson@tango.me>
Date:   Mon Jul 11 11:28:46 2016 +0300

    Hystrix*Key refactoring to extract common logic and fix race condition

    * Extract internation logic to InternMap utility class
    * Introduce common HystrixKey interface