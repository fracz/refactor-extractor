commit 72e6af034447c7ca220e7dab35ed68c87522f2f0
Author: Ruslan Kabalin <ruslan.kabalin@luns.net.uk>
Date:   Tue May 31 11:18:11 2011 +0100

    MDL-27171 messages: fix static variable filtering bug in get_message_processors

    Static $processors should contain the full list of processors only, when
    filtering is required, the $processors variable should not be updated.

    Lambda function refactoring is made as well.

    Signed-off-by: Ruslan Kabalin <ruslan.kabalin@luns.net.uk>