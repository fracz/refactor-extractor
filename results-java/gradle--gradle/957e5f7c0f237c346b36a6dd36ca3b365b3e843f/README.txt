commit 957e5f7c0f237c346b36a6dd36ca3b365b3e843f
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Nov 9 07:11:45 2011 +0100

    Move 3 more classes core->core-impl. They were only used in core-impl and those are internal implementation classes. This should also fix the compilation problem because now the abstract test is also in core-impl. At the end of the refactoring I will make a pass on 'core', there might be other classes that can be moved over to core-impl.