commit 94bea913521442361ddd39597003fc3b27636a30
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Wed Jun 20 09:45:10 2012 +0200

    improvements to slf4j.SimpleSlf4jLoggingConfigurer

    - renamed to logback.SimpleLogBackLoggingConfigurer
    - reuse underlying renderer and configurer instead of recreating them with every log level change (consistent with LogbackLoggingConfigurer, Szczepan should review)