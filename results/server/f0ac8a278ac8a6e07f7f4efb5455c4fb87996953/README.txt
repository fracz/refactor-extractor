commit f0ac8a278ac8a6e07f7f4efb5455c4fb87996953
Author: Lukas Reschke <lukas@owncloud.com>
Date:   Tue Jan 13 11:03:58 2015 +0100

    Use json_encode on string

    It's better to encode the string to prevent possible (yet unknown) bugs in combination with PHP's type juggling.

    Previously the boolean statements evaluated to either an empty string (false) or a not empty one (true, then it was 1). Not it always evaluates to false or true.

    This also removes a stray - that was not intended there but shouldn't have produced any bugs. Just to increase readability.

    Thanks @nickvergessen for spotting.

    Addresses https://github.com/owncloud/core/pull/13235/files#r22852319