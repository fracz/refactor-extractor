commit 45f11f519c37e6859e3b94ab23d9f9102ceb7df6
Author: Johan Janssens <johan@nooku.org>
Date:   Fri Aug 31 18:36:45 2007 +0000

    Added support for charsets to JView, charset is used by the JView::escape function when escaping using htmlspecialchars or htmlentities.
    Fixed com_search to use proper output escaping using JView::escape.
    Added JDocument::setBase and getBase functions to allow setting the document base uri
    JRouter has been completely refactored.
    JApplicationHelper::getClientInfo now returns a reference to the client array instead of a copy of the array. This allows to dynamically set client information at runtime
    Moved JText and JRoute to joomla.methods, to support lazy loading of JLanguage and JRouter
    Added getInstance functions to JApplication, JRouter and JPathway
    Removed JSearch class, not used in 1.5

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@8682 6f6e1ebd-4c2b-0410-823f-f34bde69bce9