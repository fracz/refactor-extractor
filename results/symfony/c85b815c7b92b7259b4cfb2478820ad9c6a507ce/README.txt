commit c85b815c7b92b7259b4cfb2478820ad9c6a507ce
Author: stloyd <stloyd@gmail.com>
Date:   Thu Jun 30 14:48:56 2011 +0200

    Fixed few issues with Date and Time:

    * TimeType:
      - seconds are no longer populated if "with_seconds" = false
      - "widget = text" is now properly rendered (closes #1480)
    * DateTimeToStringTransformer:
      - fixed using not default "format" (probably fix #1183)
    * DateType, DateTimeType, TimeType:
      - fixed "input = datetime" and test covered
      - a bit changed readability