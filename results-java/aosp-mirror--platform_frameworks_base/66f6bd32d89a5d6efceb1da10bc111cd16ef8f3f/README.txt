commit 66f6bd32d89a5d6efceb1da10bc111cd16ef8f3f
Author: Clara Bayarri <clarabayarri@google.com>
Date:   Tue Apr 26 12:18:36 2016 +0100

    Cleanup LocaleList docs given API review

    -- Remove default constructor from public API since getEmptyLocaleList exists
    -- Merge the Locale and Locale[] constructors by providing a single Locale… varargs constructor
    -- forLanguageTags, get, toLanguageTags, size, need docs
    -- get(int location) should be get(int index)

    Plus general docs improvements

    Bug: 28296200
    Change-Id: I8b4e67184f8c723daebcd251f04947d48bbb5478