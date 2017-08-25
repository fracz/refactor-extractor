commit fca2d3d7542fa6b9d15c0433d7b2bb33add29843
Author: Petr Skoda <commits@skodak.org>
Date:   Sat May 12 13:19:22 2012 +0200

    MDL-32659 potential navigation course loading improvements

    The idea is to not use inequalities in SQL queries for loading of courses in navigation because ti seems some SQL query optimiser have problems with that.