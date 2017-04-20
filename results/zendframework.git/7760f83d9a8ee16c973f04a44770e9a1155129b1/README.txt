commit 7760f83d9a8ee16c973f04a44770e9a1155129b1
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Jul 3 08:57:10 2012 -0500

    [#1713] Minor CS/flow fixes

    - use sprintf() for exception messages
    - use get_class($this) instead of get_called_class() when in non-static
      context
    - make conditionals positive when possible for readability
    - remove Zend\I18n\Filter\FilterPluginManager and put configuration in
      Zend\Filter\FilterPluginManager