commit 37c8c0fa034bc9f86a1026db01020becd603924c
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Mon Jul 4 22:54:43 2016 +0200

    Improve logging for batched cluster state updates (#19255)

    We've been slowly improving batch support in `ClusterService` so service won't need to implement this tricky logic themselves. These good changes are blessed but our logging infra didn't catch up and we now log things like:

    ```
    [2016-07-04 21:51:22,318][DEBUG][cluster.service          ] [node_sm0] processing [put-mapping [type1],put-mapping [type1]]:
    ```

    Depending on the `source` string this can get quite ugly (mostly in the ZenDiscovery area).

    This PR adds some infra to improve logging, keeping the non-batched task the same. As result the above line looks like:

    ```
    [2016-07-04 21:44:45,047][DEBUG][cluster.service          ] [node_s0] processing [put-mapping[type0, type0, type0]]: execute
    ```

    ZenDiscovery waiting on join moved from:

    ```
    [2016-07-04 17:09:45,111][DEBUG][cluster.service          ] [node_t0] processing [elected_as_master, [1] nodes joined),elected_as_master, [1] nodes joined)]: execute
    ```

    To

    ```
    [2016-07-04 22:03:30,142][DEBUG][cluster.service          ] [node_t3] processing [elected_as_master ([3] nodes joined)[{node_t2}{R3hu3uoSQee0B6bkuw8pjw}{p9n28HDJQdiDMdh3tjxA5g}{127.0.0.1}{127.0.0.1:30107}, {node_t1}{ynYQfk7uR8qR5wKIysFlQg}{wa_OKuJHSl-Oyl9Gis-GXg}{127.0.0.1}{127.0.0.1:30106}, {node_t0}{pweq-2T4TlKPrEVAVW6bJw}{NPBSLXSTTguT1So0JsZY8g}{127.0.0.1}{127.0.0.1:30105}]]: execute
    ```

    As a bonus, I removed all `zen-disco` prefixes to sources from that area.