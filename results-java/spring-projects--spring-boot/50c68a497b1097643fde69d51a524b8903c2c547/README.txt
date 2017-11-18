commit 50c68a497b1097643fde69d51a524b8903c2c547
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Fri Sep 16 16:27:03 2016 +0200

    Improve startup error message

    This commit improves the startup error message so that it does not
    reference  `--debug` anymore. Such command-line switch only works when
    the application is started using `java -jar`.

    The error message now refers directly to a section of the documentation
    that provides more details and links to more useful examples.

    Closes gh-6593