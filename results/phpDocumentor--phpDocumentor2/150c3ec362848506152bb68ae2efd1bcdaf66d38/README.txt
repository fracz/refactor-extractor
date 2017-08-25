commit 150c3ec362848506152bb68ae2efd1bcdaf66d38
Author: Mike van Riel <me@mikevanriel.com>
Date:   Fri May 30 07:27:21 2014 +0200

    Fixes #1278: Template is not read from configuration

    During a recent refactoring of the configuration system the part
    where templates got read in the commands was not updated to
    reflect the new situation.

    With this fix I have updated the retrieval of templates from the
    configuration to match the new situation.