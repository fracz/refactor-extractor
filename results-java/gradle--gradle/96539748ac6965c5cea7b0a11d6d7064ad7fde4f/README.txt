commit 96539748ac6965c5cea7b0a11d6d7064ad7fde4f
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Sat Jun 18 17:36:00 2011 +0200

    GRADLE-1532 Fixed the issue with TestNG parallel tests. The problem was caused because method name was used to identify the test id. When someone used TestNG's feature @Test(invocationCount = 2, threadPoolSize = 2) then just the method name was not enough for identification. My fix is using the ITestResult from TestNG as an identifier for internal use in a hashmap. Also I did very simple refactoring that should help in debugging similar issues in future.