commit 702f2896089a0e7e213a979e0f2b71782f20a469
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Mon Sep 16 11:52:53 2013 -0400

    Serial performance test refactoring

    * Added configurable number of edge labels to GraphGenerator

    * Added primary keys to GraphGenerator's edge labels (only simple keys
      are implemented right now -- no composites)

    * Added gremlin-groovy dependency to titan-test and added a
      Gremlin.load() static call in GroovySerialTest

    * Added a test on vertex-centric indexing using a primary-keyed edge
      label in GroovySerialTest

    * Started refactoring GroovySerialTest to look less like Java and more
      like Groovy, but that needs additional work