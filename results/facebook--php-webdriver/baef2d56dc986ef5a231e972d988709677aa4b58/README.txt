commit baef2d56dc986ef5a231e972d988709677aa4b58
Author: whhone <whhone@fb.com>
Date:   Thu Jul 25 13:17:19 2013 -0700

    refactor and simplify

    It is bad to rewrite execute() and pass the $session_id and $executor
    everytime when constructing a new WebDriver class. It increases the
    friction when building the api. This diff does not break the interface
    of all methods. Just Simplify the constructor and reuse the execute().

    Changes:
    - remove execute() in each WebDriverXXXX
    - use $driver to construct WebDriverXXXX