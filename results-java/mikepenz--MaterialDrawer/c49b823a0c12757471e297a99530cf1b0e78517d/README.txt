commit c49b823a0c12757471e297a99530cf1b0e78517d
Author: Mike Penz <mikepenz@gmail.com>
Date:   Tue Mar 3 23:43:54 2015 +0100

    * sorry for not doing a proper commit :D
    * remove the old Profile. We now use the IProfile interface for this ;)

    * add new methods and functionalities to the Header
    ** withSelectionFirstLine, withSelectionSecondLine <-- define the text in the drawer header
    ** withHeaderScaleType
    ** withProfileImagesClickable
    ** withSelectionListEnabledForSingleProfile (set false if you don't want the selection list for a single profile)
    ** withSelecitonListEnabled (set false if you don't want the selection list at all) :D
    ** some fixes to the source
    ** change from Profile to IProfile
    ** new getter & setter for the Result, getHeaderBackgroundView, setBackground, setBackgroundRes

    * improve the BaseDrawerAdapter and fix a potential exception
    * add ProfileSettingDrawerItem

    * improve CircularImageView and add elevation support for Lollipop +

    * new values for the new item