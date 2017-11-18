commit 3c334e44d63d1409995f13a7efacdccecb2c5ee7
Author: Emmanuel Rodriguez <emmanuel.rodriguez@gmail.com>
Date:   Thu Sep 24 23:26:34 2015 +0200

    Improved JSConsole when handling "%d" with a float as argument

    In JavaScript every number is a floating point number, even ints! This causes
    console.log("%d", 42) to fail because the initial implemenation was using
    String.format() and it can't handle a floating point with %d.

    We now have a custom formatter that mimics what google chrome does (with a few
    improvemnts).

    This patch also adds unit tests to the project stetho-js-rhino.