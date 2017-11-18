commit 95f2c19780de4d065eef45bdaf4bd95fd0874f60
Author: Krisztián Nagy <okt.valdar@gmail.com>
Date:   Sun Dec 18 16:08:59 2016 +0100

    Sonar bug fixes using Yoda condition in equals expression when comparing String literal with String object.
    Using try-with-resources if we use Scanner to close the underlying stream is a good practice to handle resources.
    Minimal refactor.