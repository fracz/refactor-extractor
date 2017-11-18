commit 7601394b7b2e5d8f18726dcbd7136b6608d040d6
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Tue Jan 28 11:14:04 2014 +0100

    Add two new system notifications, for clock skew and missing master in cluster

    also refactor the notification class slightly to guard against missing timestamp and node id

    fixes Graylog2/graylog2-server#401
    fixes Graylog2/graylog2-server#402
    fixes Graylog2/graylog2-server#403