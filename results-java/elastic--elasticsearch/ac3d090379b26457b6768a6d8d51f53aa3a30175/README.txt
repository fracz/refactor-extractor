commit ac3d090379b26457b6768a6d8d51f53aa3a30175
Author: Martijn van Groningen <martijn.v.groningen@gmail.com>
Date:   Sat Jul 11 00:16:33 2015 +0200

    Added date math support in index names

    Date math index name resolution enables you to search a range of time-series indices, rather than searching all of your time-series indices and filtering the the results or maintaining aliases. Limiting the number of indices that are searched reduces the load on the cluster and improves execution performance. For example, if you are searching for errors in your daily logs, you can use a date math name template to restrict the search to the past two days.

    The added `ExpressionResolver` implementation that is responsible for resolving date math expressions in index names. This resolver is evaluated before wildcard expressions are evaluated.

    The supported format: `<static_name{date_math_expr{date_format|timezone_id}}>` and the date math expressions must be enclosed within angle brackets. The `date_format` is optional and defaults to `YYYY.MM.dd`. The `timezone_id` id is optional too and defaults to `utc`.

    The `{` character can be escaped by places `\\` before it.

    Closes #12059