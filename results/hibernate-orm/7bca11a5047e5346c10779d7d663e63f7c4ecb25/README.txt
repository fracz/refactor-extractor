commit 7bca11a5047e5346c10779d7d663e63f7c4ecb25
Author: Anton Marsden <anton.marsden@ninetyten.co.nz>
Date:   Fri Aug 2 22:13:32 2013 +1200

    HHH-4577: Improved performance of ActionQueue

    The ActionQueue has been seriously refactored. An ExecutableList class
    has been created to manage each list of actions, and it includes
    serialization behaviour that was previously in ActionQueue. Prevalidate
    behaviour has changed - prevalidate is now called once per execution
    list rather than once per execution. A test case has been added for
    ExecutableList. There is also a new method on the Executable interface.