commit a17703138b0c51226c0e73dd8d34ea037d260aeb
Author: Dan Sandler <dsandler@android.com>
Date:   Fri Jul 10 13:59:29 2015 -0400

    Allow notification strings to be unredacted in dump output.

    This won't happen automatically and unredacted strings will
    still not appear in bugreports, but if you are attached via
    adb you can now do `dumpsys notification --unredact` to get
    the contents of String/CharSequence fields and extras
    emitted along with the rest of the dump.

    The arg handling is also improved so that multiple filters
    can be specified at once, e.g. --package (was "p") to
    restrict to a single package, and --stats to get JSON
    output.

    Bug: 20451514
    Change-Id: I37fc5ce86c7e28e8f8286917f6764d64bc081108