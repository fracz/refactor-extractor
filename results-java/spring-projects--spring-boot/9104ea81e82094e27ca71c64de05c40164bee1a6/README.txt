commit 9104ea81e82094e27ca71c64de05c40164bee1a6
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Mon Dec 19 14:06:17 2016 +0100

    Expose Validator bean

    This commit improves ValidationAutoConfiguration so that a `Validator`
    bean is exposed if JSR 303 is available. This has the side effect of
    automatically enable Spring Data Couchbase's entity validation rather
    than requiring to expose a `Validator` bean.

    Closes gh-5178