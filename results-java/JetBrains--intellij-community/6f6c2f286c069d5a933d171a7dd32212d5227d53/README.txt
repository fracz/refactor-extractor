commit 6f6c2f286c069d5a933d171a7dd32212d5227d53
Author: Roman Chernyatchik <roman.chernyatchik@jetbrains.com>
Date:   Wed Oct 19 15:30:57 2011 +0400

    1. OSProcessHandler & Python process handlers were refactored (code duplication, etc.) 2. RubyProcessHandler - Kill process tree recursively 3. "soft-kill' feature for Python & Ruby process handlers