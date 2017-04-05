commit 617423797d403b66f91d5761c367061427cf03f0
Author: Oliver Gierke <ogierke@gopivotal.com>
Date:   Fri May 3 13:48:39 2013 +0200

    DATAJPA-340 - Updated template.mf to generate and improved OSGi manifest.

    Opened up the upper bound for Spring to be able to work with Spring 4. Expanded lower boundary of Slf4j dependency to 1.6.4 to let STS 3.x work with the JAR out of the box.