commit 07182b8f7cea144e420b90d8b80386651df906e6
Author: Johan Janssens <johan@nooku.org>
Date:   Mon Oct 15 21:14:45 2007 +0000

    Performance improvements : The following changes allow autoloading of framework files
    using JLoader and jimport. They create forwards compatibilty with PHP 5.x and prpvide
    significant performance improvements.
     - Possible breakages are expected to occur, please take this into account
     - Moved JMail into it's own package
     - Moved JMailHelper into it's own file
     - Moved JPlugin and JPluginHelper into their own package
     - Renamed JEventDispatcher to JDispatcher
     - Renamed JEventHandler to JEvent
     - Renamed JBufferStream to JBuffer
     - Renamed joomla.utilities.array to joomla.utilities.arrayhelper
     - Renamed i18n package to language package
     - Moved archive.tar to pear.archive_tar.Archive_Tar
     - Removed joomla.utilities.compat.phputf8env
     - Added import file that imports all the needed framework classes
     - Optimised the legacy plugin to use autoload, this reduces memory usage with +/- 1 MB
    Fixed [#7561] Legacy extensions fail on mosMainFrame

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@9233 6f6e1ebd-4c2b-0410-823f-f34bde69bce9