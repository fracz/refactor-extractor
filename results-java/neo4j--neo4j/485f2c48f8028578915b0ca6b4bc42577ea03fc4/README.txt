commit 485f2c48f8028578915b0ca6b4bc42577ea03fc4
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Thu Oct 2 14:24:01 2014 +0200

    Cache values on reads

    - refactor ConcurrentTrackerState to have an internal map rather than extends a ConcurrentHashMap