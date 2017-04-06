commit e9f67a7246b53b5a837af69db13e26e2f78646d0
Merge: 1ecfd44 47a822d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Dec 22 16:13:29 2013 +0100

    minor #9841 [Tests|WCM] add memcache, memcached, and mongodb extensions to run skipped tests (cordoval)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    [Tests|WCM] add memcache, memcached, and mongodb extensions to run skipped tests

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | na
    | License       | MIT
    | Doc PR        | na

     - [x] go over all skipped tests, take note and check they are reasonable
     - [x] reenable memcache, mongodb, and memcached

    We are keeping the icu intl related tests skipped because setting up icu 51.2 is totally time consuming in travis and it would require a custom distro box on travis because there are no ppa's available for the ubuntu version. I tried hard but it does not seem worth it. Same for plugging beta of memcached with pecl, it is just not reasonable to be running beta versions on travis. This then does not address #9797 but at least now we are aware.

    This PR now can be merged as is as it improves tests that before were not ran. Not all but more than before. :baby:

    Commits
    -------

    47a822d add memcache, memcached, and mongodb extensions to run skipped tests