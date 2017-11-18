commit 436737a077cc17c6a66e52df6d5d6a5bb7d3cae3
Author: Chris Gioran <chris@neotechnology.com>
Date:   Wed Aug 3 12:54:54 2016 +0300

    DatasourceManager lifecycle is now exclusively controlled by LocalDatabase

    DatasourceManager lifecycle was controlled in part by the top level LifeSupport
     and in part by CoreState which, through LocalDatabase, started and stopped
     DSM to download snapshots. Now DSM is controlled only through LD, allowing
     for simpler lifecycle dependencies and future improvements in caching StoreId,
     blocking message processing etc.