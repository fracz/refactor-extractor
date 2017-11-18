commit 7b5471d3bf8c12f03a1212c112c0dd264320899d
Author: Chris Gioran <chris@neotechnology.com>
Date:   Wed Apr 22 22:10:34 2015 +0300

    Fixed masking of startup exception with shutdown exception in IAGD

    This is a forward port of #4458 from 2.2, adjusted to the new
     realities of the refactoring around IAGD to Facade and friends.