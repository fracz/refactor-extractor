commit ec4177884f7e7103d6195f4a8e34a99796fe3812
Author: Aleksey Pivovarov <AMPivovarov@gmail.com>
Date:   Mon Dec 28 17:18:35 2015 +0300

    vcs: do not set dialog title twice

    fix error, introduced by refactoring in 1375b0e - setTitle() call order was changed