commit 35f1d84daf0b71488f7513906088ef2dd47d97e8
Author: Gabriel Rodrigues Couto <gabrielrcouto@gmail.com>
Date:   Tue Mar 29 22:16:30 2016 -0300

    Some refactors on IPC Receiver (splitMessage, removeDebug and jsonDecode)
    OutputDebug turned off on Lazarus (was causing a memory leak after too many continuos messages, reported by @reisraff)