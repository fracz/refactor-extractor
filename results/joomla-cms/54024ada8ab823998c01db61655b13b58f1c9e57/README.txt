commit 54024ada8ab823998c01db61655b13b58f1c9e57
Author: Johan Janssens <johan@nooku.org>
Date:   Thu Nov 16 23:15:13 2006 +0000

    Removed JController::setViewName, use JController::getView instead
    Implemented factory method in JController::getView to be able to handle multiple views
    Added JPath::find function to easily search a array of paths for a certain file
    General MVC improvements to simply component implementations that follow conventions

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@5800 6f6e1ebd-4c2b-0410-823f-f34bde69bce9