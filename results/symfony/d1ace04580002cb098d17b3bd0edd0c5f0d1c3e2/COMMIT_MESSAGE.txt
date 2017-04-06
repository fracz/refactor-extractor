commit d1ace04580002cb098d17b3bd0edd0c5f0d1c3e2
Merge: ad8d872 4b9d74d
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Dec 22 16:57:41 2013 +0100

    minor #9830 [WCM][Tests] Improve tests running with specific requirements (except intl icu) (cordoval)

    This PR was submitted for the 2.4-dev branch but it was merged into the 2.4 branch instead (closes #9830).

    Discussion
    ----------

    [WCM][Tests] Improve tests running with specific requirements (except intl icu)

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

    6e0b2dc added condition to avoid skipping tests on JSON_PRETTY support