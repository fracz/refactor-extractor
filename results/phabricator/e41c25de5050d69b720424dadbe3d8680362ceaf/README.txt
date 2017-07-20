commit e41c25de5050d69b720424dadbe3d8680362ceaf
Author: Mukunda Modell <mmodell@wikimedia.org>
Date:   Sun Mar 26 08:16:47 2017 +0000

    Support multiple fulltext search clusters with 'cluster.search' config

    Summary:
    The goal is to make fulltext search back-ends more extensible, configurable and robust.

    When this is finished it will be possible to have multiple search storage back-ends and
    potentially multiple instances of each.

    Individual instances can be configured with roles such as 'read', 'write' which control
    which hosts will receive writes to the index and which hosts will respond to queries.

    These two roles make it possible to have any combination of:

    * read-only
    * write-only
    * read-write
    * disabled

    This 'roles' mechanism is extensible to add new roles should that be needed in the future.

    In addition to supporting multiple elasticsearch and mysql search instances, this refactors
    the connection health monitoring infrastructure from PhabricatorDatabaseHealthRecord and
    utilizes the same system for monitoring the health of elasticsearch nodes. This will
    allow Wikimedia's phabricator to be redundant across data centers (mysql already is,
    elasticsearch should be as well).

    The real-world use-case I have in mind here is writing to two indexes (two elasticsearch clusters
    in different data centers) but reading from only one. Then toggling the 'read' property when
    we want to migrate to the other data center (and when we migrate from elasticsearch 2.x to 5.x)

    Hopefully this is useful in the upstream as well.

    Remaining TODO:

    * test cases
    * documentation

    Test Plan:
    (WARNING) This will most likely require the elasticsearch index to be deleted and re-created due to schema changes.

    Tested with elasticsearch versions 2.4 and 5.2 using the following config:

    ```lang=json
      "cluster.search": [
        {
          "type": "elasticsearch",
          "hosts": [
            {
              "host": "localhost",
              "roles": { "read": true, "write": true }
            }
          ],
          "port": 9200,
          "protocol": "http",
          "path": "/phabricator",
          "version": 5
        },
        {
          "type": "mysql",
          "roles": { "write": true }
         }
      ]

    Also deployed the same changes to Wikimedia's production Phabricator instance without any issues whatsoever.
    ```

    Reviewers: epriestley, #blessed_reviewers

    Reviewed By: epriestley, #blessed_reviewers

    Subscribers: Korvin, epriestley

    Tags: #elasticsearch, #clusters, #wikimedia

    Differential Revision: https://secure.phabricator.com/D17384