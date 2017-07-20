commit 2cf1d707adb79d308f515dd3fe5dbccdb3337a47
Author: Samuel Georges <sam@daftspunk.com>
Date:   Sat Aug 8 11:17:56 2015 +1000

    The API response for insert / insert+crop should be identical
    This is because the 'onInsert' callback is used for both actions, yet yielded different results causing breakages
    Fixes #1281
    Also improved code readability in some places