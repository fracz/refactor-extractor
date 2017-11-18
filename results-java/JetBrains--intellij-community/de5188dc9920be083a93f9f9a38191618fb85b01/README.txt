commit de5188dc9920be083a93f9f9a38191618fb85b01
Author: Kirill Likhodedov <Kirill.Likhodedov@gmail.com>
Date:   Fri Jul 8 20:48:41 2011 +0400

    Git tests refactoring.

    1. Remove GitSingleUserTest and GitCollabarativeTest: in most tests
       collaboration is tested, and declaring 3 repositories doesn't slow
       down tests too much.
    2. Instead all 3 repositories (IDEA project, origin and another child)
       are defined in GitTest.
    3. GitTestRepository works with java.io.File instead of VirtualFile
       to reduce refresh usages.
    4. Running Git is made from GitTestRunEnv - this simplifies some methods.