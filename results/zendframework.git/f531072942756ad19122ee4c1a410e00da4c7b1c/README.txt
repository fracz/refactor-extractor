commit f531072942756ad19122ee4c1a410e00da4c7b1c
Author: Ralph Schindler <ralph.schindler@zend.com>
Date:   Thu Jun 21 12:41:14 2012 -0500

    Zend\Db features & refactorings:
    * ResultSet interface and abstract created
    * Default ResultSet uses array or ArrayObject solution
    * HydratingResultSet added
    * Driver\ResultInterface altered to include getFieldCount() method
    * All Zend\Db classed fixed to use ResultSetInterface and/or proper implementation
    * Removed Row object and interface from ResultSet