commit 020074cbec8f72ba89a3ae32254374ec3ac43183
Author: Mike Penz <mikepenz@gmail.com>
Date:   Fri Mar 27 00:42:31 2015 +0100

    * improve new imageloader logic
    ** stop loading image if there was a new drawable set (prevent overwriting drawables if the imageloader takes longer)
    * move init for the DrawerImageLoader logic to a custom application