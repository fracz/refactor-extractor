commit 2cd179d40ee431dcbf0b7e65696bb61046943e0f
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri May 18 18:10:18 2012 +0400

    [merge] refactoring: use structure instead of array indices

    * MergeFragment: hold TextRanges in named fields instead of operating an array via indices 0, 1, 2.
    * cleanup, set @NotNull/@Nullable, fix code style.