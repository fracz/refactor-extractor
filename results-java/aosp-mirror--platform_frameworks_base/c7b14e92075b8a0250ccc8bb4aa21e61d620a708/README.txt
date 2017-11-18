commit c7b14e92075b8a0250ccc8bb4aa21e61d620a708
Author: Suchi Amalapurapu <asuchitra@google.com>
Date:   Wed May 27 14:11:50 2009 -0700

    Fix a bug in AppSecurityPermissions where it wouldn't display permissions used by an app if it uses a shared user id.
    Remove the else clause and always get the list of requested permissions first before adding the permissions obtained via
    the shared user id.
    Also change an if condition and comments for better readability