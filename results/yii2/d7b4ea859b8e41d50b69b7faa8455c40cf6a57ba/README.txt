commit d7b4ea859b8e41d50b69b7faa8455c40cf6a57ba
Author: Carsten Brandt <mail@cebe.cc>
Date:   Mon Jun 16 18:58:36 2014 +0200

    refactored date formatting functions

    - removed unformat methods, they do not belong here
    - removed db format, which is also not purpose of this class
    - refactored the whole set of methods to be simpler and better
      maintainable

    More unit tests needed.