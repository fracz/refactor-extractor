commit 4a06daf2c31530cc8f5cc517158361e641849084
Merge: caabd41 7d61154
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Mar 27 08:58:32 2014 +0100

    feature #9818 [TwigBundle] Add command to list twig functions, filters, globals and tests (Seldaek)

    This PR was squashed before being merged into the 2.5-dev branch (closes #9818).

    Discussion
    ----------

    [TwigBundle] Add command to list twig functions, filters, globals and tests

    Sample output:

    ```
    $ app/console twig:doc date
    Functions
      date(date = null, timezone = null)

    Filters
      date(format = null, timezone = null)
      date_modify(modifier)
    ```

    JSON output also available, could be helpful for IDEs.

    Possible improvement would be to read the first line of the docblock of each method/function to show a bit more info.

    Feedback welcome on the command's name. Not really convinced but too late to think of something better.

    Credits to @gagarine for the great idea

    Commits
    -------

    7d61154 Add command to list twig functions, filters, globals and tests