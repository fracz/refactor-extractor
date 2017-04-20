commit e9c00b1bae2f8e4ffe875fc1002439ac3c8b55db
Author: Cheng Lou <chenglou92@gmail.com>
Date:   Wed Apr 16 10:53:05 2014 -0700

    Remove grunt-complexity

    The report itself is more or less useful because it detects stuff like
    big objects (e.g. CSSProperty) as being too complicated. Furthermore, afaik
    nobody refactors the code based on what the report says =).