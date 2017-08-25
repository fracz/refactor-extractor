commit 308329b5479d16e104c9eb7372222fee67ccdbf5
Author: Marc Alexander <admin@m-a-styles.de>
Date:   Thu Nov 14 15:09:49 2013 +0100

    [ticket/11896] Minor code improvements in phpbb_functional_test_case

    Use assertContainsLang() and get rid of unnecessary logic in create_post()
    and create_topic(). The docblocks were also slightly improved.

    PHPBB3-11896