commit c9b94195120f626e9c686464aca85004da762838
Author: Richard Fath <richard67@users.noreply.github.com>
Date:   Wed Jul 5 18:06:52 2017 +0200

    Move core extensions array from script.php to a new helper class to provide a safe way to check if core extension (#16724)

    * Move core extensions list to new helper class

    Move the static list of core extensions used in script.php for updating
    the manifest caches to a new helper class for making it possible to be
    used by other functions, too, and add a function for check if an
    extension is core or not.
    This provides a safe way to detect core extensions, while checking if
    extension ID is greater than a certain value is not a safe way.

    * Change order of parameters, add default values

    Change order of parameters and add default values for function
    checkIfCoreExtension().

    * Simplify checkIfCoreExtension

    * Add unit tests

    * Correct subpackage in doc block

    Correct subpackage in doc block of the unit tests.

    * Alpha order the sections and improve their comments

    Alpha order the sections by `type`, `element`, `folder`, 'admin'/'site'
    and impove the section comments for better maintainability as suggested
    by @brianteeman

    * Remove unit test

    Remove unit test as recommended by @rdeutz