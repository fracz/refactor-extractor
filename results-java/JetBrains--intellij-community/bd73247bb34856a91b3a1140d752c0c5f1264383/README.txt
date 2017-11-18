commit bd73247bb34856a91b3a1140d752c0c5f1264383
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Wed Apr 27 12:22:34 2011 +0400

    Perforce: 1) refactor a bit to remove some unneeded refreshes 2) refactor login subsystem to don't re-query logged state periodically, but listen to p4tickets file changes 3) correctly add-dispose notifications
     also will fix
    IDEA-68719 Perforce: in multi-root project Changes view is updated endlessly (status bar blinking) after first login