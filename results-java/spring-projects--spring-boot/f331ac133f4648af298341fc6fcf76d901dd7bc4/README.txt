commit f331ac133f4648af298341fc6fcf76d901dd7bc4
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Tue Feb 14 15:30:43 2017 +0100

    Add reactive web server infrastructure

    This commit adds the infrastructure for creating and customizing
    reactive embedded web servers. Common configuration has been refactored
    into the new `ConfigurableEmbeddedWebServer` interface.

    See gh-8302