commit 3f9e1323800271656bafb27e661a95ef140ee3fc
Author: Alexey Kudravtsev <cdr@intellij.com>
Date:   Wed Oct 4 19:11:25 2017 +0300

    refactor registerExtension() to avoid using Element.getParent() which precluded important optimisations