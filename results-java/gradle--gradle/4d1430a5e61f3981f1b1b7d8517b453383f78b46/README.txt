commit 4d1430a5e61f3981f1b1b7d8517b453383f78b46
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Wed Jun 20 09:42:02 2012 +0200

    improved filtering of log levels in logback

    - make logger.isEnabled(level) return correct results by using TurboFilter instead of Filter
    - renamed slf4j.Slf4jLoggingConfigurer to logback.LogbackLoggingConfigurer
    - reuse same filter instead of installing a new one at every log level change (assumed to be safe because the same is already done for appender)
    - code cleanup