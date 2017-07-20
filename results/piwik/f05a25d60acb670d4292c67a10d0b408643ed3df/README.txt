commit f05a25d60acb670d4292c67a10d0b408643ed3df
Author: Thomas Steur <thomas.steur@googlemail.com>
Date:   Sat Oct 4 11:28:23 2014 +0200

    moved some queries within core to model files

    This is only a start. Especially the Tracker model contains quite a bunch of different models. Ideally, one day, the tracker is refactored and all the goal tracker stuff including the queries are in the goal plugins, all the ecommerce stuff is in an ecommerce plugin etc. Haven't moved some of the LogAggregator queries.