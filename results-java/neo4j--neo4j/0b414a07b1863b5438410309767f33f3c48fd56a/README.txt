commit 0b414a07b1863b5438410309767f33f3c48fd56a
Author: Jim Webber <jim@neotechnology.com>
Date:   Thu Dec 23 12:54:46 2010 +0000

    Changing the URIs in the config file now actually does change the URIs where services are offered.

    Note: although the tests go green here, I haven't investigated how much this might affect Webadmin (though I did refactor its AdminPropertiesService to use the NeoServer data, so it should be ok). Famous. Last. Words.

    Merry Christmas!

    git-svn-id: https://svn.neo4j.org/components/server/trunk@8104 0b971d98-bb2f-0410-8247-b05b2b5feb2a