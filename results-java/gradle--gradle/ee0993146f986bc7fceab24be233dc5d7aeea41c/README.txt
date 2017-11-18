commit ee0993146f986bc7fceab24be233dc5d7aeea41c
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Mar 21 22:56:06 2011 +0100

    After merging idea & eclipse subproject I moved integTests accordingly. This refactoring makes it much easier to run only the ide-specific tests if one changes only eclipse/idea plugin. Needed to move one common base class to core fixtures.