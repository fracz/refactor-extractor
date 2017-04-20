commit 3e6594fe34468491aa9cc378fae76e981e60c960
Author: Ralph Schindler <ralph.schindler@zend.com>
Date:   Thu Aug 16 18:08:10 2012 -0500

    Zend\Db\RowGateway
    * RowGateway refactor primary key handling
    * Enable delete() method
    * Minimize memory footprint of stored data
    * Add internal API for tracking existing rows