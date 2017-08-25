commit 116f23745bf863964e772ba21d7432b51732d2b5
Author: Gabriel Rodrigues Couto <gabrielrcouto@gmail.com>
Date:   Tue Oct 4 12:48:39 2016 -0300

    IPC Protocol modification, now each message is terminated by a NULL character.
    Huge performance improvement on OSX.
    Canvas example is really fast now.