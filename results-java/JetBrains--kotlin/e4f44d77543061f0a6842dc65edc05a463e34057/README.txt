commit e4f44d77543061f0a6842dc65edc05a463e34057
Author: Dmitry Kovanikov <kovanikov@gmail.com>
Date:   Fri Sep 18 18:42:17 2015 +0300

    [cli-repl] Small refactoring
    Add `flushHistory` to ReplCommandReader
    Pass `WhatNextAfterOneLine` to `readLine`