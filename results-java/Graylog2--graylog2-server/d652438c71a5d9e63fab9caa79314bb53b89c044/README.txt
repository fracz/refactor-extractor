commit d652438c71a5d9e63fab9caa79314bb53b89c044
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Wed Oct 15 18:34:08 2014 +0200

    refactored input metrics handling

    transports now return a MetricSet instead of registering the metrics directly, which avoid leaking the MessageInput instance into the metric registration