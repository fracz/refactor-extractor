commit d2d39921b56fca54b85702d212ed357abc1010e2
Author: diosmosis <benaka@piwik.pro>
Date:   Sun Mar 1 20:28:14 2015 -0800

    Do not use missing getIniFile() exception message in Config, instead catch the exception and augment message w/ file being read in IniFileChain. Also includes some mild refactoring to IniFileChain.