commit 7e1cffb50a4ee58b54c91cdae45ab0437b7325e8
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Aug 10 14:52:25 2017 +0300

    shelf: implement silent unshelve to selected changelist using dnd

    * extract methods to appropriate managers;
    * fix update method contract: return true if drop is not possible;
    * refactor update method for shelved drag bean case;
    * IDEA-169729 add ability to choose changelist while dragging
     shelved changes to 'Local Changes' tab;