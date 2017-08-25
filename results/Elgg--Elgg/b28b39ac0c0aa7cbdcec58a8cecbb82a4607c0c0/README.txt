commit b28b39ac0c0aa7cbdcec58a8cecbb82a4607c0c0
Author: Jerôme Bakker <jeabakker@coldtrick.com>
Date:   Fri Apr 10 14:08:19 2015 +0200

    chore(thewire): improved error handling when removing a wire post

    Make sure we have a correct entity type before removing it from the
    database

    fixes #7003