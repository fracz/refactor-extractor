commit 99d2f27c8475f7b3cffe2e0265ca2d35120a99e0
Author: Shay Banon <kimchy@gmail.com>
Date:   Tue Jul 10 00:47:37 2012 +0200

    Introduce Text abstraction, allowing for improved representation of strings, apply to HighlightedField (breaks backward for Java API from String to Text), closes #2093.
    By introducing the Text abstraction, we can keep (long) text fields in their UTF8 bytes format, and no need to convert them to a string when serializing it back to Json for example.

    The first place we can apply this is to highlighted text, which can be long.. . This does breaks backward comp. for people using the Java API where the HighlightField now has a Text as its content, and not String.