commit c9a92001151b4663e1f17b54feadf12e0ce237ac
Merge: 311a9bd 3917ed7
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jul 4 20:26:31 2011 +0200

    merged branch stloyd/datetime_fixes (PR #1485)

    Commits
    -------

    3917ed7 Revert "* DateType, DateTimeType, TimeType: - a bit changed readability"
    c85b815 Fixed few issues with Date and Time:

    Discussion
    ----------

    [Form] Fixed few issues with Date and Time

    Fixed few issues with Date and Time:

    * TimeType:
      - seconds are no longer populated if "with_seconds" = false
      - "widget = text" is now properly rendered (closes #1480)
    * DateTimeToStringTransformer:
      - fixed using not default "format" (probably fix #1183)
    * DateType, DateTimeType, TimeType:
      - fixed "input = datetime" and test covered