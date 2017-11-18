commit 5aa71f709d68d3425b9b8e6f1e6536a788f47bd9
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri May 25 16:10:29 2012 +0200

    improvements to test logging

    - changed defaults
    - logging of exceptions, causes, and stack traces can now be turned on and off individually
    - added option for "short" exception logging
    - don't show common stack trace elements between cause and parent trace
    - improved implementation of entry point stack trace filter
    - stack trace filters are now based on org.gradle.api.specs.Spec
    - formatting tweaks
    - added more tests