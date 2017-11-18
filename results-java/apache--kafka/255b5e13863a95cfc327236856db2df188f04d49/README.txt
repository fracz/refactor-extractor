commit 255b5e13863a95cfc327236856db2df188f04d49
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Tue Mar 22 19:13:26 2016 -0700

    KAFKA-3431: Remove `o.a.k.common.BrokerEndPoint` in favour of `Node`

    Also included a minor efficiency improvement in kafka.cluster.EndPoint.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Gwen Shapira

    Closes #1105 from ijuma/kafka-3431-replace-broker-end-point-with-node