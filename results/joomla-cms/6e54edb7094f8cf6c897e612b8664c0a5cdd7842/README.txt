commit 6e54edb7094f8cf6c897e612b8664c0a5cdd7842
Author: Johan Janssens <johan@nooku.org>
Date:   Tue Sep 11 20:27:59 2007 +0000

    Removed getPublicProperties from JObject and moved to mosDBTable legacy functions
    Added JObject::setProperties and JObject::getProperties
    Refactored JUser to use JObject setError and improved flexibility of the user data store through JUser::getTable function. This change allows to easily extend the user database tables

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@8841 6f6e1ebd-4c2b-0410-823f-f34bde69bce9