commit 507e954d0088f4ae4eae29fcf34a5ca1cf33c1de
Author: Mark Vieira <portugee@gmail.com>
Date:   Fri Jul 31 01:13:49 2015 -0600

    Resource transport credentials refactoring.
     - Authentication objects are now passed thru to ResourceConnectionFactory
     - Connectors now use credentials on Authentication objects for configuration
     - Removed some usages of duplicate PasswordCredentials class in resources project

    +review REVIEW-5569