commit 8542448f20e947e5b66876074923f97308e49031
Author: Carsten Brandt <mail@cebe.cc>
Date:   Fri Nov 22 17:29:05 2013 +0100

    refactored redis AR to relect the latest changes

    - make use of traits
    - short array
    - better implementation of query findByPk