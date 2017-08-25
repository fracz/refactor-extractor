commit 24993280edcf66f9daa5a5e82428fefef4a3ab56
Author: Bjoern Schiessle <schiessle@owncloud.com>
Date:   Thu Dec 4 19:51:04 2014 +0100

    Next step in server-to-server sharing next generation, see #12285

    Beside some small improvements and bug fixes this will probably the final state for OC8.

    To test this you need to set up two ownCloud instances. Let's say:

    URL: myPC/firstOwnCloud user: user1
    URL: myPC/secondOwnCloud user: user2
    Now user1 can share a file with user2 by entering the username and the URL to the second ownCloud to the share-drop-down, in this case "user2@myPC/secondOwnCloud".

    The next time user2 login he will get a notification that he received a server-to-server share with the option to accept/decline it. If he accept it the share will be mounted. In both cases a event will be send back to user1 and add a notification to the activity stream that the share was accepted/declined.

    If user1 decides to unshare the file again from user2 the share will automatically be removed from the second ownCloud server and user2 will see a notification in his activity stream that user1@myPC/firstOwnCloud has unshared the file/folder from him.