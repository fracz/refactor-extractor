commit 4f2ebed676c201ca050e1dda0c1a9b561342ad5a
Author: Tom Morris <tfmorris@gmail.com>
Date:   Wed Sep 18 13:16:24 2013 -0400

    Make localization language list dynamic - fixes #807
    - refactor LoadLanguageCommand so language loading can be reused
    - add GetLanguagesCommand for the server
    - change GUI to fetch language list and update selection list with it