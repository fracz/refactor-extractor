commit af91f21b5c5c03696699822354767a28b6362d23
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Thu Mar 31 12:17:02 2016 +0300

    Speedup AssetServletTest

    Starting and stopping `ServletTester` are very expensive operations,
    because they tied with the Jetty servlet container and require working
    with OS resources, which by definition is slow.

    Actually we can reuse one server for all tests, because its state doesn't
    change between test runs. This improves overall test suite throughput.