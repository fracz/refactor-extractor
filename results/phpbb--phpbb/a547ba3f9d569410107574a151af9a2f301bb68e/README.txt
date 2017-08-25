commit a547ba3f9d569410107574a151af9a2f301bb68e
Author: Marc Alexander <admin@m-a-styles.de>
Date:   Tue May 14 19:44:55 2013 +0200

    [ticket/11538] Use regex for testing color value and improve tests

    We are now using a regex with preg_match() in order to properly check
    if the entered color value is in hex color format or not. A proper
    error message is triggered if an incorrect color value is entered and
    the prepended '#' is removed if necessary.

    PHPBB3-11538