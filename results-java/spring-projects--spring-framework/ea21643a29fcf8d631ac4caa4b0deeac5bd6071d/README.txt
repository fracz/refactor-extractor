commit ea21643a29fcf8d631ac4caa4b0deeac5bd6071d
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Fri Jun 10 12:26:06 2016 +0200

    Various DataBuffer improvements

    - Added fromIndex parameter to indexOf and lastIndexOf
    - Moved DataBuffer.tokenize to StringEncoder, as that's the only place
      it's used.