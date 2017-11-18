commit 55bb1789aa0fd5b776f18dc133e699dd1f634165
Author: oleksiys <oleksiys@mytec>
Date:   Wed Sep 28 19:18:33 2011 -0700

    + refactoring the GrizzlyAsyncHttpProvider to support partial request body transferring.
    + @TODO pass context to BodyGenerator so it will be able to continue payload transferring.