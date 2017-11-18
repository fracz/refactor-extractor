commit b27c137087ee926746b43b1abd08fb360ede671b
Author: Narayan Kamath <narayan@google.com>
Date:   Tue Jul 1 10:56:20 2014 +0100

    Fix broken assumptions in LocalePicker.

    This code assumed that the locale is always 5 chars in
    length and was of the form xx-YY. This is not necessarily
    true. The language can be 3 letters in length and the locale
    might have a script and variant.

    Also cleans up several nasty pieces of code and eliminates
    unnecessary array copies and improves readability by using
    idiomatic java.

    bug: 15873165
    bug: 10090157

    Change-Id: Iab1cfd7b78e5772b1245654f2153caf63a96033d