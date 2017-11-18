commit 27270ec583a19b4aff66760d0b23dcfaf2d6d4f5
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Jan 23 13:11:05 2012 +0100

    Fixed the problem with slf4j static configuration in the tooling api provider...

    Now we configure the static slf4j logging for tooling api provider only once per provider implementation (every provider implementation lives in safe environment - e.g. separate classloader). This has limitations, e.g. once the logging level has been set, you cannot change it for the same provider implementation (DefaultConnection). However, I don't think this is a big deal because provider api is pretty thin (not much logging). The internal verboseLogging property is still used by our tests and might be used by clients for troubleshooting. We still can change log levels for the build execution - however at the moment we don't offer log level configurable in the tooling api.

    -rationalized some code around embeddable logging
    -improved the related coverage