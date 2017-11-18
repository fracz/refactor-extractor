commit c9463efd7b83ea9a72f9524eaef6a428d887cc07
Author: Rene Groeschke <rene@breskeby.com>
Date:   Mon Feb 20 00:14:14 2012 +0100

    introduce FileOrUriNotationParser
    - refactored FileNotationParser to FileOrUriNotationParser
    - refactored AbstractFileResolver to use FileOrUriNotationParser to resolve URIs and Files