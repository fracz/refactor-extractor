commit aaabd17038054fa44f3c0e68ec67ffbd2dc6b984
Author: Tyson Andre <tysonandre775@hotmail.com>
Date:   Sat Apr 29 10:01:10 2017 -0700

    Miscellaneous fixes and improvements for daemon mode

    Followup of #563

    If a file is re-parsed in daemon mode,
    then clear the previous parse errors in that file.
    Otherwise, they'd keep showing up after they were fixed.

    Add options for emacs flycheck plugin to work with phan
    (Allows substituting a file in /tmp/ for a file in the project)
    (linked in the original PR for daemon mode).
    An AST class node's name is null for an anonymous class.
    - --temporary-file-map (JSON mapping of multiple files)
      and --flycheck-file (For use with a single file (for only one file
      provided))
      (Inconvenient to JSON encode in flycheck)

    Add --disable-plugins to the server, which speeds up quick mode even more if
    lower latency for daemon mode requests is desired in a large project.

    Make phan_client compatible with php 5.6 to reduce setup needed to test
    phan_client in daemon mode.