commit 615019be055586ff5d5d05d0006da70dc82e0498
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Sun Jan 29 19:15:43 2012 +0000

    Issue 189 about preparing the sub-query and TRAVERSE operator. First step done by refactoring SQL query to have a TARGET property as Iterable<OIdentifiable>. In this way any iterable can be the target where to execute the filtering (WHERE clause) and selecting fields (PROJECTIONS).