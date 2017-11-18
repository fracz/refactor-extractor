commit af882cbd51f010d2f46845d2f136f28ab282ce41
Author: Andrey Breslav <andrey.breslav@jetbrains.com>
Date:   Mon Jul 2 20:31:07 2012 +0400

    Dependency injection frameworks improvements
    * Injectors can now implement interfaces (this was the primary goal)
    * Fields can have different types than getters
    * Imports are more accurate and no duplicate imports are added