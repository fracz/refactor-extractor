commit 0ab8b4726acd9d7c803cded2a345bd7219352f43
Author: Bernd Ahlers <bernd@graylog.com>
Date:   Wed Feb 4 19:46:58 2015 +0100

    Fix performance regression with process buffer handling.

    Before this all process buffer processor threads had to wait for a single
    decoding processor. This caused a huge performance drop especially on
    radio nodes.

    This can be refactored to be a bit nicer in upcoming versions. We tried
    to minimize the change for 1.0.0.

    Fixes #944.