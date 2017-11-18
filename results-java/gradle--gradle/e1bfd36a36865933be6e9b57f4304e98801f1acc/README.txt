commit e1bfd36a36865933be6e9b57f4304e98801f1acc
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Dec 6 17:06:42 2011 +0100

    GRADLE-1933 Tooling api thread safety.

    Added explicit unit test coverage around shared connector services (though we have integ test coverage for that). It is very useful because it documents some of the design choices around tacking the tooling api concurrency. It should also give better pointers (than the integ test) in case some refactoring breaks the thread safety.