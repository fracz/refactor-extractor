commit 15bede5a634c996191d1929caaacaa7e42cf18d0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon May 23 16:41:34 2011 +0200

    [Console] refactored style management

    The current code was broken when a style was defined inline:

      <bg=black>Foo</bg=black>

    When creatin a new style formatter, it's better to let the formatter
    apply the style to the text.