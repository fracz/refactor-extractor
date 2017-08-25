commit 9c3710854bdca479faac1fc7bd74bc12e67c389c
Author: Marina Glancy <marina@moodle.com>
Date:   Thu May 26 17:18:13 2016 +0800

    MDL-53598 block_glossary_random: do not fail if glossary was deleted

    This commit refactors how associated glossary is searched for and removes
    unnecessary DB queries. Also prevents from situations when the global glossary or course have
    been deleted