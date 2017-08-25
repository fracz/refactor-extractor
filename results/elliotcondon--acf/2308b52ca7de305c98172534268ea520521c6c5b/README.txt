commit 2308b52ca7de305c98172534268ea520521c6c5b
Author: Elliot Condon <e@elliotcondon.com>
Date:   Sun Dec 16 12:42:03 2012 +1100

    Fixed bug with WYSIWYG where inserting an attachment would appear in the wrong editor if the cursor had not been place inside the editor body.

    * Updated the $field['id'] value to be much smart
    * Updated repeater and flexible content field to use acfcloneindex instead of 999 and improved the JS for adding rows

    Signed-off-by: Elliot Condon <e@elliotcondon.com>