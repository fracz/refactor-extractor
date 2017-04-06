commit 990fb4638af82034f0680feae22fd07a3ed6586a
Merge: 3228d50 fa20b51
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jul 6 15:14:36 2011 +0200

    merged branch everzet/console-help-printing-fix (PR #1553)

    Commits
    -------

    fa20b51 [Console] refactored definition printer
    e49d61f [Console] fixed console help printing expectations

    Discussion
    ----------

    [Console] definitions printing fix

    Before this change, console component printed definitions as:

    ``` bash
    Arguments:
     features        Feature(s) to run. Could be a dir (features/),
    a feature (*.feature) or a scenario at specific line
    (*.feature:10).

    Options:
     --config (-c) Specify external configuration file to load. behat.yml or config/behat.yml will be used by default.
     --profile (-p) Specify configuration profile to use. Define profiles in config file (--config).
     --init Create features directory structure.

     --format (-f) How to format features. pretty is default. Available formats are
    - pretty
    - progress
    - html
    - junit
     --out Write formatter output to a file/directory instead of STDOUT (output_path).
     --colors Force Behat to use ANSI color in the output.
     --no-colors Do not use ANSI color in the output.
     --no-time Hide time in output.
     --lang Print formatter output in particular language.
     --no-paths Do not print the definition path with the steps.
     --no-snippets Do not print snippets for undefined steps.
     --no-multiline No multiline arguments in output.
     --expand Expand Scenario Outline Tables in output.

     --story-syntax Print *.feature example in specified language (--lang).
     --definitions Print available step definitions in specified language (--lang).

     --name Only execute the feature elements (features or scenarios) which match part of the given name or regex.
    ```

    As you might see, indentation is totally broken (also notice how it prints multiline descriptions in argument and --format option).

    This PR makes output looks like this:

    ``` bash
    Arguments:
     features        Feature(s) to run. Could be a dir (features/),
                     a feature (*.feature) or a scenario at specific line
                     (*.feature:10).

    Options:
     --config (-c)   Specify external configuration file to load. behat.yml or config/behat.yml will be used by default.
     --profile (-p)  Specify configuration profile to use. Define profiles in config file (--config).
     --init          Create features directory structure.

     --format (-f)   How to format features. pretty is default. Available formats are
                     - pretty
                     - progress
                     - html
                     - junit
     --out           Write formatter output to a file/directory instead of STDOUT (output_path).
     --colors        Force Behat to use ANSI color in the output.
     --no-colors     Do not use ANSI color in the output.
     --no-time       Hide time in output.
     --lang          Print formatter output in particular language.
     --no-paths      Do not print the definition path with the steps.
     --no-snippets   Do not print snippets for undefined steps.
     --no-multiline  No multiline arguments in output.
     --expand        Expand Scenario Outline Tables in output.

     --story-syntax  Print *.feature example in specified language (--lang).
     --definitions   Print available step definitions in specified language (--lang).

     --name          Only execute the feature elements (features or scenarios) which match part of the given name or regex.
    ```