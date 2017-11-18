commit 69da73f10a571dfc146288da6c352a74d6d8d135
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Dec 9 21:20:45 2011 +0100

    Standard input for tooling api - refactoring...

    Cleaned up some code around tooling api compatibility WRT consuming standard input. I was considering introducing LongRunningOperationParametersVersion2. However, since I couldn't get without conditional logic anyway, I decided to use reflection to check if protocol instance supports given method. Reflection seems more lightweight since we already use in tooling api and we have very comprehensive tests anyway. @Adam, give feedback :)