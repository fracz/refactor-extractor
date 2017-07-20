commit e37ab385f5c9ef8824d2ad4e31f544dbe6089095
Author: Derek Allard <derek.allard@ellislab.com>
Date:   Tue Feb 3 16:13:57 2009 +0000

    DB count_all() not returns an integer always
    Added some syntactical improvements within DB (braces)
    Fixed a bug when doing 'random' on order_by() (#5706).
    Fixed a bug where adding a primary key through Forge could fail (#5731).
    Fixed a bug when using DB cache on multiple databases (#5737).