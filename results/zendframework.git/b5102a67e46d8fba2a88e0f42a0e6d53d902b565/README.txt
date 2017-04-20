commit b5102a67e46d8fba2a88e0f42a0e6d53d902b565
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Tue May 25 12:01:37 2010 -0400

    [signals] renamed and refactored SignalSlots

    - s/Messenger/SignalSlot/
    - Refactored API to follow more generic naming conventions for SignalSlots
    - all tests for basic functionality now pass
    - todo: move FilterChain into Zend\Filter or Zend\StdLib