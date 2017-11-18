commit ad81a1140f351ec19165cb94d5aa668cfc08a932
Author: Daniel Trebbien <dtrebbien@gmail.com>
Date:   Sun Aug 20 18:21:16 2017 -0400

    Utilize parameterized logging

    As suggested in the SLF4J FAQ:
    https://www.slf4j.org/faq.html#logging_performance
    .. parameterized logging can improve the efficiency of logger calls when
    logging at the specified level is disabled.

    According to the Constant Expressions section of the JLS, enum variants
    are unfortunately not constant, and thus this is actually a string
    concatenation.

    This change was suggested by SLF4J Helper for NetBeans IDE:
    http://plugins.netbeans.org/plugin/72557/