commit 8e6f1218e15ebc25a985ef86cd608593318ba721
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Tue Feb 7 11:55:08 2017 +0100

    Detect Reactive web applications

    This commit improves `@ConditionalOnWebApplication` to specify the
    requested web application type. By default, any web application will
    match but it can also match only if a recactive (or servlet) web
    application is present.

    Closes gh-8118