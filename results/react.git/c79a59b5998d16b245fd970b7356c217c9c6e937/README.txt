commit c79a59b5998d16b245fd970b7356c217c9c6e937
Author: Paul O’Shannessy <paul@oshannessy.com>
Date:   Mon Jun 17 10:52:16 2013 -0700

    Improve JSXTransformer

    The biggest improvement is that we'll now insert each parsed JSX script
    back into a `<script>` tag with the body set. This allows the browser to
    execute these scripts normally. Using `Function(functionBody)` or
    `eval(functionBody)` both execute in window scope, but `var` assignments
    don't actually get set on window (unlike everywhere else).

    I also did some cleanup to make the code a little bit more readable.
    In my minimal test cases this didn't break anything (scripts loaded in
    the right order).