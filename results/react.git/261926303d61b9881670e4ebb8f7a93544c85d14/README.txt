commit 261926303d61b9881670e4ebb8f7a93544c85d14
Author: Ben Alpert <spicyjalapeno@gmail.com>
Date:   Tue Dec 31 13:37:15 2013 -0700

    Remove sometimes-confusing console error

    Fixes #767. This essentially reverts 738de8c.

    We could store some sort of flag to silence the console error here but since we've made significant improvements in markup wrapping, this error is fairly rare now. We'll also have validation of node structure soon in #735.