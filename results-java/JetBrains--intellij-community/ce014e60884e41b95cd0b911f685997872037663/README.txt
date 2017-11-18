commit ce014e60884e41b95cd0b911f685997872037663
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon Aug 18 03:23:06 2014 +0400

    RemoteTarget validation improved

    * validation method api changed: now  only user's string input validation performed for selected repo;
    * not null target created only if user's input is valid;
    * postpone validation for dialog switched off;
    * request focus added to repository panel renderer;
    * tree root became inline;
    * annotations added