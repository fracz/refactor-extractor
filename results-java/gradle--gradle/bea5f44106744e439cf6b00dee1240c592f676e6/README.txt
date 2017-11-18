commit bea5f44106744e439cf6b00dee1240c592f676e6
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Dec 14 13:07:58 2011 +0100

    Tooling api thread safety coverage fixes...

    Made sure each test starts with a clean state. Now the test runs should be consistent regardless if all tests or single test is ran. Some refactorings in the tests.