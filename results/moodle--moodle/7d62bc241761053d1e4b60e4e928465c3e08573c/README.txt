commit 7d62bc241761053d1e4b60e4e928465c3e08573c
Author: David Mudrák <david@moodle.com>
Date:   Tue Sep 15 22:08:04 2015 +0200

    MDL-51403 install: Improve the language validation in the CLI installer

    There are two essential improvements here. Firstly, by replacing the
    file_exists() check with array_key_exists() we make sure that only
    actual language code will be written into the config.php file (and not
    the empty value in case of input that does not pass the PARAM_SAFEDIR
    cleaning).

    Additionally, we no longer display the full list of available languages
    by default. The list can be displayed in the interactive mode by typing
    the ? character instead of the language code. This makes the overall
    interface cleaner, does not cause the header information (such as the
    Moodle version) to scroll away and makes the nice cli logo more visible
    (which was the main motivation for the whole patch anyway ;-).