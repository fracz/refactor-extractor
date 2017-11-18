commit c13b47482a94387b90ca3a43dd5168f9195aa413
Author: Valentina Kiryushkina <valentina.kiryushkina@jetbrains.com>
Date:   Fri Jun 17 15:49:24 2016 +0300

    Fix according to review IDEA-CR-11509: make adaptive courses more discoverable

    1) Always show public adaptive courses in combobox
    2) If adaptive course is selected force user to login on Stepic
    3) If user creates adaptive course and doesn't enrolled in it enroll him silently
    4) Login dialogs minor ui improvements