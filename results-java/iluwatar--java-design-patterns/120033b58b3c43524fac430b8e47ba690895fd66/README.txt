commit 120033b58b3c43524fac430b8e47ba690895fd66
Author: Krisztián Nagy <okt.valdar@gmail.com>
Date:   Sun Dec 18 16:04:13 2016 +0100

    Sonar bug fixes using Yoda condition in equals expression when comparing String literal with String object.
    Using try-with-resources if we use Scanner to close the underlying stream is a good practice to handle resources.
    Minimal refactor.