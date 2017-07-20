commit d39b7084cd8052fcba096bef0d887e43d05194e0
Author: Ryan Weaver <ryan@thatsquality.com>
Date:   Fri May 13 15:42:38 2011 -0500

    Trying to carefully improve the language on a common exception message

    For example, if using Symfony, this is probably that first thing you'll see after installing, because the logs directory won't be writable. So, ideally, it should be clear enough that the user knows to change the permissions on the directory. Many developers might not understand the idea of a "stream", but they do understand a file not being writable.