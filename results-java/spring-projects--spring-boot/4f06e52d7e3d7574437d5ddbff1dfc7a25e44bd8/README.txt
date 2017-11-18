commit 4f06e52d7e3d7574437d5ddbff1dfc7a25e44bd8
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Mon Oct 24 14:14:55 2016 +0200

    Allow adding additional tld skip patterns

    This commit improves `TomcatEmbeddedServletContainerFactory` so that tld
    skip patterns can be set or added to an existing set. An additional
    `server.tomcat.additional-tld-skip-patterns` is now being exposed to
    easily add patterns via configuration.

    Closes gh-5010