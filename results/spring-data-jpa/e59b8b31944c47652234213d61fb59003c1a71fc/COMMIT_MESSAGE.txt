commit e59b8b31944c47652234213d61fb59003c1a71fc
Author: Oliver Gierke <info@olivergierke.de>
Date:   Mon Aug 13 10:28:12 2012 +0200

    DATAJPA-243 - QueryDslJpaRepository applies sort given through Pageable correctly.

    During the refactoring of the Querydsl interaction a regression of not applying a Sort wrapped in a Pageable was introduced in QueryDsl utility class.