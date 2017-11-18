commit 75b29e306fc88295c975271b0132b74313e41641
Author: Alistair Jones <alistair.jones@gmail.com>
Date:   Fri Sep 23 16:03:27 2016 +0100

    Client connector config improvements.

    * Move HTTP config from neo4j-server to neo4j-dbms.
    * Better defaults for dbms.connector.
    * bolt, http and https connectors all enabled by default.
    * Add tests cover old behaviour, and show that new behaviour is
      unsurprising.
    * Remove unnecessary lines from neo4j.conf.