commit a3d5830a0a40611934927998f9b8596f033e55fe
Author: Petr Skoda <commits@skodak.org>
Date:   Sat Mar 31 23:51:02 2012 +0200

    MDL-32149 PHPUnit test support - part 2

    Includes:
    * constants refactoring
    * reworked db table init
    * support for $CFG->debug = -1
    * functional DB tests
    * fixed $DB->get_indexes() to not throw exceptions when table does not exist
    * fix handling of user passwords in test db
    * add debug info to exception messages
    * removed unnecessary PHP debug errors from mathslib
    * fixed @error suppression in get_string
    * fixed PHPUnit error handler setup
    * added timezone info to default install