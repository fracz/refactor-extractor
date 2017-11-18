commit 1af1078057cd636a0f650125fc926ca1c1f4e74e
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Jun 30 20:23:23 2016 +0300

    [shelf]: deleteProvider refactoring: create one smarter delete provider instead of 2 separated

    * IDEA-102021 - remove strategy improved;
    * delete all changelists selected directly (list nodes) with all changes inside;
    * delete all separately selected changes ( recycle from normal lists and completely delete  from already recycled lists);
    * improve confirmation message;