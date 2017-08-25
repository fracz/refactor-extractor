commit 49dddc7cc6eb90f06d7ced98275124c373ec15e8
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sat Jan 12 16:41:47 2013 +0100

    Fix #696: Reenable plugin generator and update generated files

    During a previous refactoring an error had been introduced that did not enable
    the plugin tasks. I have reenabled the tasks and updated the GenerateCommand
    to match the new coding standards and fix any bugs that were contained in it.