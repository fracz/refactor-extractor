commit b19d31e0671011d8a97c94cb4572ea14ed87eb79
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Fri Jan 20 12:08:32 2017 +0100

    Use a random port with embedded Mongo by default

    This commit improves the logic of the embedded Mongo support to use a
    random port if no custom port has been specified. This doesn't change
    the default if the embedded support isn't active.

    Closes gh-8044