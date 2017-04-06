commit 554b28d9754521936bce928c1453e19e1da15ee3
Merge: 65c9aca 0d1a58c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 3 10:45:47 2014 +0100

    feature #10356 [Console] A better progress bar (fabpot)

    This PR was merged into the 2.5-dev branch.

    Discussion
    ----------

    [Console] A better progress bar

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #9573, #9574, #10187, #9951, related to #9788
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#3626

    TODO:

     - [x] add some docs

    See what this PR allows you to do easily:

    ![cwzpvk](https://f.cloud.github.com/assets/47313/2302190/30cd80e4-a170-11e3-8d88-80c4e4ca8b23.gif)

    ## New ProgressBar class

    First, this PR deprecates `ProgressHelper` in favor of `ProgressBar`. The main difference is that the new `ProgressBar` class represents a single progress bar, which allows for more than one bar to be displayed at a time:

    ```php
    use Symfony\Component\Console\Helper\ProgressBar;
    use Symfony\Component\Console\Output\ConsoleOutput;

    $output = new ConsoleOutput();

    $bar1 = new ProgressBar($output, 10);
    $bar2 = new ProgressBar($output, 20);
    $bar2->setProgressCharacter('#');
    $bar1->start();
    print "\n";
    $bar2->start();

    for ($i = 1; $i <= 20; $i++) {
        // up one line
        $output->write("\033[1A");
        usleep(100000);
        if ($i <= 10) {
            $bar1->advance();
        }
        print "\n";
        $bar2->advance();
    }
    ```

    And here is how it looks like when run:

    ![progress-bars](https://f.cloud.github.com/assets/47313/2300612/4465889a-a0fd-11e3-8bc2-b1d2a0f5dc3d.gif)

    ## Format Placeholders

    This pull request refactors the way placeholders in the progress bar are managed. It is now possible to add new placeholders or replace existing ones:

    ```php
    // set a new placeholder
    ProgressBar::setPlaceholderFormatterDefinition('remaining_steps', function (ProgressBar $bar) {
        return $bar->getMaxSteps() - $bar->getStep();
    });

    // change the behavior of an existing placeholder
    ProgressBar::setPlaceholderFormatterDefinition('max', function (ProgressBar $bar) {
        return $bar->getMaxSteps() ?: '~';
    });
    ```

    Several new built-in placeholders have also been added:

     * `%remaining%`: Display the remaining time
     * `%estimated%`: Display the estimated time of the whole "task"
     * `%memory%`: Display the memory usage

    ## Formats

    Formats can also be added (or built-in ones modified):

    ```php
    ProgressBar::setFormatDefinition('simple', '%current%');

    $bar->setFormat('simple');
    // is equivalent to
    $bar->setFormat('%current%');
    ```

    Built-in formats are:

     * `quiet`
     * `normal`
     * `verbose`
     * `quiet_nomax`
     * `normal_nomax`
     * `verbose_nomax`

    ## Format Messages

    You can also set arbitrary messages that depends on the progression in the progress bar:

    ```php
    $bar = new ProgressBar($output, 10);
    $bar->setFormat("%message% %current%/%max% [%bar%]");

    $bar->setMessage('started');
    $bar->start();

    $bar->setMessage('advancing');
    $bar->advance();

    $bar->setMessage('finish');
    $bar->finish();
    ```

    You are not limited to a single message (`message` being just the default one):

    ```php
    $bar = new ProgressBar($output, 10);
    $bar->setFormat("%message% %current%/%max% [%bar%] %end%");

    $bar->setMessage('started');
    $bar->setMessage('', 'end');
    $bar->start();

    $bar->setMessage('advancing');
    $bar->advance();

    $bar->setMessage('finish');
    $bar->setMessage('ended...', 'end');
    $bar->finish();
    ```

    ## Multiline Formats

    A progress bar can now span more than one line:

    ```php
    $bar->setFormat("%current%/%max% [%bar%]\n%message%");
    ```

    ## Flexible Format Definitions

    The hardcoded formatting for some placeholders (like `%percent%` or `%elapsed%`) have been removed in favor of a `sprintf`-like format:

    ```php
    $bar->setFormat("%current%/%max% [%bar%] %percent:3s%");
    ```

    Notice the `%percent:3s%` placeholder. Here, `%3s` is going to be used when rendering the placeholder.

    ## ANSI colors and Emojis

    The new progress bar output can now contain ANSI colors and.or Emojis (see the small video at the top of this PR).

    Commits
    -------

    0d1a58c [Console] made formats even more flexible
    8c0022b [Console] fixed progress bar when using ANSI colors and Emojis
    38f7a6f [Console] fixed PHP comptability
    244d3b8 [Console] added a way to globally add a progress bar format or modify a built-in one
    a9d47eb [Console] added a way to add a custom message on a progress bar
    7a30e50 [Console] added support for multiline formats in ProgressBar
    1aa7b8c [Console] added more default placeholder formatters for the progress bar
    2a78a09 [Console] refactored the progress bar to allow placeholder to be extensible
    4e76aa3 [Console] added ProgressBar (to replace the stateful ProgressHelper class)