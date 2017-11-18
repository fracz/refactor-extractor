commit a631e5322cb8ab9f8e7905e1380b1a2991bb9957
Author: anna <Anna.Kozlova@jetbrains.com>
Date:   Wed Mar 21 17:45:33 2012 +0100

    leaks in inplace refactorings: make MyExpression class static in order to release references on outer class (cherry picked from commit 4fc0c37)