commit baf744b1d8e291a6cba2e143134e2209d9625590
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Tue Mar 10 14:07:29 2015 +0300

    PY-9679 Several UI improvements in "Move Module Members" dialog

    * Table with module members is collapsable and hidden by default.
    User preference about its state is remembered in PropertiesComponent.
    * Message in the top of the dialog more precisely describes refactoring:
    if only one element was selected and table was hidden initially, this
    message doesn't mention "selected elements". This way new dialog
    resembles the old one, when only one element is going to be moved.