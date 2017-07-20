commit 7644e86683464805305b338404ee2f4d90cda286
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Apr 22 12:35:09 2011 +0200

    refactored session configuration

     * made the options array only for "global" options that are valid for all session storages
     * changed the PDO session storage constructor signature to accept an array of options for DB configuration
     * changed the storage_id to be the full service id, instead of just part of it
     * removed the class parameter for session as it can be changed via the .class parameter (it was the only example in the framework)
     * removed the configuration for the PDO session storage for now