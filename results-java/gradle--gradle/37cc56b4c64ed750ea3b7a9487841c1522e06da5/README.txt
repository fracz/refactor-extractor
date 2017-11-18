commit 37cc56b4c64ed750ea3b7a9487841c1522e06da5
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri Apr 27 13:03:30 2012 +0200

    first batch of improvements to test console output

    - introduced AbstractTestLogger, added support for colored logging
    - introduced TestTraceLogger and TestExceptionLogger
    - turned TestLogging from interface into class
    - added new configuration options to TestLogging
    - print report path as file URL so that report can easily be opened in browser