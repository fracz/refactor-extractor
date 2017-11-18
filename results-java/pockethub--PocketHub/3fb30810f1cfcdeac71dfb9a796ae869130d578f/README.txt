commit 3fb30810f1cfcdeac71dfb9a796ae869130d578f
Author: Roberto Tyley <roberto@github.com>
Date:   Fri Mar 30 13:00:21 2012 +0100

    Fix #18 : avatars duplicated on initial load

    This fix switches the implementation of AvatarHelper (which used the
    'tag' field of the ImageView for tracking loaded updates) to use
    https://github.com/rtyley/lazy-drawables (which uses a custom drawable
    that updates itself to the same end).

    some other small improvements:

    * keys off gravatar id rather than the avatar_url supplied by the GitHub
    api. This allows us to show avatars against commits, even though those
    commits may have been made by people who aren't GitHub users, and allows
    us to use the same pool of avatar images for both
    * avatar images in the in-memory cache are pre-scaled to the right size
    for the ImageView, which saves the android platform from having to do it
    whenever the avatar is re-used - helps performance on scrolling thru big
    lists
    * uses streaming on download to avoid allocating a full byte array for
    the compressed image data
    * does a bit more work (decompressing image data) on the non-UI thread,
    to avoid jerkiness

    also closes #5