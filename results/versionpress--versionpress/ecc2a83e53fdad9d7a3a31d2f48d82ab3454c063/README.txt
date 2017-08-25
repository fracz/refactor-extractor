commit ecc2a83e53fdad9d7a3a31d2f48d82ab3454c063
Author: Borek Bernard <borekb@gmail.com>
Date:   Mon Nov 24 20:25:41 2014 +0100

    Several rename refactorings:

     * src/Mirror -> src/storages/Mirror
     * src/Initializer -> src/initialization/Initializer
     * src/change-infos/* -> src/changeinfos/* (see also [#187])
     * tests/wp-cli -> tests/end2end (see also [#187])