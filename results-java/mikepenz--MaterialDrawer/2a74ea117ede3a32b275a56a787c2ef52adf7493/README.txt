commit 2a74ea117ede3a32b275a56a787c2ef52adf7493
Author: Mike Penz <mikepenz@gmail.com>
Date:   Wed Jul 15 23:01:37 2015 +0200

    * add proof of concept of a Gmail like MiniDrawer, including a CrossFade util which handles those two views
    ** this is not meant to be used in production yet.
    ** it needs some hard refactoring
    ** i will move out the Crossfade functionality
    ** i will have to add a RecyclerView instead of the ScrollView
    ** the hard links between Drawer, Header, MiniDraewr are not yet optimal
    ** minimize the logic which s required by the dev at the moment to handle close and so on