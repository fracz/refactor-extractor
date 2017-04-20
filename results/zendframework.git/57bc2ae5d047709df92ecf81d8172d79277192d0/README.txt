commit 57bc2ae5d047709df92ecf81d8172d79277192d0
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Dec 13 17:38:10 2011 -0600

    Began refactoring Adapters to use options objects

    - Created base AdapterOptions for options used with all adapters
    - Created FilesystemOptions implementation for Filesystem adapter
    - Currently untested