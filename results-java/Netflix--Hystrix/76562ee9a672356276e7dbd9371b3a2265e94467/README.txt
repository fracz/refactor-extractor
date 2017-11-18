commit 76562ee9a672356276e7dbd9371b3a2265e94467
Author: Patrick Ruhkopf <patrick.ruhkopf@bertelsmann.de>
Date:   Tue Sep 22 11:44:15 2015 -0400

    Added example demonstrating ObservableCollapser

    This was based on the discussion in https://github.com/Netflix/Hystrix/issues/895. I've modified the original example to be compatible with JDK 6, removed the dependency to ICU and refactored the test class to be part of the command class to match the pattern used for other examples.