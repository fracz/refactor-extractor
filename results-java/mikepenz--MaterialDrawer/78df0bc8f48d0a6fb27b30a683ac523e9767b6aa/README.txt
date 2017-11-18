commit 78df0bc8f48d0a6fb27b30a683ac523e9767b6aa
Author: Mike Penz <mikepenz@gmail.com>
Date:   Thu Aug 13 20:49:00 2015 +0200

    * add new helper method to set the vertical padding for the drawerItems as there is a bug with API Level 17 which ignores the set padding via xml
    * improve layout of the PrimaryDrawerItem to be more performant and better support RTL