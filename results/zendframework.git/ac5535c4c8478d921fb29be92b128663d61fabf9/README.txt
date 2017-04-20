commit ac5535c4c8478d921fb29be92b128663d61fabf9
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Thu Apr 19 17:16:38 2012 -0500

    [#944] re-enable StaticEventManager awareness

    - Re-enables StaticEventManager awareness temporarily until
      Application/Bootstrap is refactored to allow sharing a
      SharedEventManager instance and injecting into many discrete
      EventManager instances.