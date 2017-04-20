commit c5f71f3a083c8b25a293a42ebffd288ccd0d914b
Author: prolic <saschaprolic@googlemail.com>
Date:   Sun Jul 8 14:31:35 2012 +0200

    ZF2-387 classmap_generator.php does not detect multiple classes in a single file

    added support for multiple classes in a single php file
    added support for multiple namespaces in a single php file
    added tests
    refactored classmap-generator