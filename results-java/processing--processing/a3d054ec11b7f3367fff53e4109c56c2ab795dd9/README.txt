commit a3d054ec11b7f3367fff53e4109c56c2ab795dd9
Author: Jakub Valtar <jakub.valtar@gmail.com>
Date:   Thu Aug 27 15:51:03 2015 -0400

    FX - improve key events

    - handle PRESSED and RELEASED for ".", "/", "*", "-" and "+" and numeric
    keys
    - prevent AIOOBE for all events