commit deb39f43824e5a3b118dcc8f75220f533ed09c65
Author: tjhunt <tjhunt>
Date:   Tue Nov 11 02:26:03 2008 +0000

    user selector: MDL-16966 Nicer messages when there are too many users to show.

    Also, improved the comment that explains to subclassers what they have to do, and fixed get_selected_users, so if the number of selected users is greater than the max number of users to show, it still works.