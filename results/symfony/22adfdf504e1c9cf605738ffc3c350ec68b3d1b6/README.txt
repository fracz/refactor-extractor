commit 22adfdf504e1c9cf605738ffc3c350ec68b3d1b6
Merge: a7c9863 8de7813
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Apr 7 16:24:42 2013 +0200

    merged branch umpirsky/console-helper-table (PR #6368)

    This PR was squashed before being merged into the master branch (closes #6368).

    Discussion
    ----------

    [2.3] [Console] TableHelper

    When building a console application it may be useful to display tabular data.

    `TableHelper` can display table header and rows, customizable alignment of columns, cell padding and colors.

    Basic usage example:
    ```php
    $table = $app->getHelperSet()->get('table');
    $table
        ->setHeaders(array('ISBN', 'Title', 'Author'))
        ->setRows(array(
            array('99921-58-10-7', 'Divine Comedy', 'Dante Alighieri'),
            array('9971-5-0210-0', 'A Tale of Two Cities', 'Charles Dickens'),
            array('960-425-059-0', 'The Lord of the Rings', 'J. R. R. Tolkien'),
            array('80-902734-1-6', 'And Then There Were None', 'Agatha Christie'),
        ))
    ;
    $table->render($output);
    ```
    Output:
    ![table](https://f.cloud.github.com/assets/208957/14955/6fb4f500-46ca-11e2-8435-0f6b22f96e58.png)

    If this PR gets merged I will submit doc PR as well.

    I'm sure there is a plenty of room for improvements so any feedback is welcome.

    Commits
    -------

    8de7813 [2.3] [Console] TableHelper