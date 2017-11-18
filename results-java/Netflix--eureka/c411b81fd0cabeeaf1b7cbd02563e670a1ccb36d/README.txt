commit c411b81fd0cabeeaf1b7cbd02563e670a1ccb36d
Author: Daniel Trebbien <dtrebbien@gmail.com>
Date:   Sat Jul 22 23:11:43 2017 -0700

    Utilize parameterized logging and remove calls to toString()

    As suggested in the SLF4J FAQ: https://www.slf4j.org/faq.html#logging_performance
    parameterized logging can improve the efficiency of logging calls when
    logging at the specified level is disabled.

    In addition, per the FAQ: https://www.slf4j.org/faq.html#paramException
    since SLF4J 1.6.0 it is possible to use parameterized logging and also
    log an exception/throwable.

    toString() is called automatically: https://www.slf4j.org/faq.html#string_contents