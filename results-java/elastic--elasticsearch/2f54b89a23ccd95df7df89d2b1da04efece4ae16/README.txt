commit 2f54b89a23ccd95df7df89d2b1da04efece4ae16
Author: Alexander Reelsen <alexander@reelsen.net>
Date:   Tue Jul 21 13:54:18 2015 +0200

    CLITool: Port PluginManager to use CLITool

    In order to unify the handling and reuse the CLITool infrastructure
    the plugin manager should make use of this as well.

    This obsolets the -i and --install options but requires the user
    to use `install` as the first argument of the CLI.

    This is basically just a port of the existing functionality, which
    is also the reason why this is not a refactoring of the plugin manager,
    which will come in a separate commit.