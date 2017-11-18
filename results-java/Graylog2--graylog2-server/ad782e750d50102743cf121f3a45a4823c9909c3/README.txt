commit ad782e750d50102743cf121f3a45a4823c9909c3
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Tue Jan 28 11:14:04 2014 +0100

    Add two new system notifications, for clock skew and missing master in cluster

    also refactor the notification class slightly to guard against missing timestamp and node id

    fixes Graylog2/graylog2-server#401
    fixes Graylog2/graylog2-server#402
    fixes Graylog2/graylog2-server#403