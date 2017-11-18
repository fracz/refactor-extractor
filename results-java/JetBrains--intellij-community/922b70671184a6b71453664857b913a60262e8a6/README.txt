commit 922b70671184a6b71453664857b913a60262e8a6
Author: Roman Chernyatchik <roman.chernyatchik@jetbrains.com>
Date:   Wed Oct 19 15:31:10 2011 +0400

    1. OSProcessHandler & Python process handlers were refactored (code duplication, etc.) 2. RubyProcessHandler - Kill process tree recursively 3. "soft-kill' feature for Python & Ruby process handlers