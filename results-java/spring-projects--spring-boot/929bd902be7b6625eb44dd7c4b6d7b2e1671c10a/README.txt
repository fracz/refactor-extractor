commit 929bd902be7b6625eb44dd7c4b6d7b2e1671c10a
Author: Oliver Gierke <info@olivergierke.de>
Date:   Wed Jan 29 18:24:49 2014 +0100

    Upgraded to Spring Data Codd

    Update AbstractRepositoryConfigurationSourceSupport to use the newly
    introduced RepositoryConfigurationDelegate instead of effectively
    reimplementing Spring Data Commons functionality which was prone to
    changes in the API (code that wasn't considered to be API in the first
    place).

    Switch from implementing BeanClassLoaderAware to ResourceLoaderAware
    to avoid having to set up a DefaultResourceLoader which should also
    improve IDE integration.

    Fixes gh-236