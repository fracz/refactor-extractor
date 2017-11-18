commit 9dd11032d0d8a8f16a9795ceabacfe067bebc0dd
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Fri Feb 26 00:40:16 2016 -0600

    Add minimal yes/no tracking of major features used to UDC.

    - Expand usage data collection to track yes/no on if the following
      features have been in use in the past four days:
      - Bolt Protocol
      - HTTP Cypher Endpoint
      - HTTP Batch Endpoint
      - HTTP TX Endpoint

    As always, this is easily turned off by disabling UDC in configuration.
    This will help improve the product by getting better data on which
    features are popular and which features see almost no use, which in
    turn can help guide further investigation to improve the overall UX
    of Neo4j.