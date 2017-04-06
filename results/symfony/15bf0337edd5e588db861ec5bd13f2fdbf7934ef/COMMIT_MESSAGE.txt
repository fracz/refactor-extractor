commit 15bf0337edd5e588db861ec5bd13f2fdbf7934ef
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Fri Mar 8 13:06:24 2013 +0100

    [FrameworkBundle] fix router debug command

    - use dedicated Route:getMethods, getSchemes
    - pattern -> path
    - show missing scheme requirement
    - show missing host regex
    - refactoring