commit f93d205e30dcb5a20b28d1fcbe3f46054054b152
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue Feb 1 14:28:38 2011 -0500

    Method renaming and related refactoring of EventManager

    - Interfaces renamed from "Dispatcher" to "Collection" to better indicate
      purpose
    - s/emit/trigger/
    - s/connect/attach/
    - Added attachAggregate() and detachAggregate() methods to simplify story around
      aggregate usage.
    - Allow parameters passed to an Event to be an object, and for set/getParam()
      methods to allow interaction with object properties.
    - Static event manager no longer allows aggregates (don't make sense in that
      context)
    - Added docblocks to all interface methods