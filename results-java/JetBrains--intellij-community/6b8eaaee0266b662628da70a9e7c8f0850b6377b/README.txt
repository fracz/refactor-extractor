commit 6b8eaaee0266b662628da70a9e7c8f0850b6377b
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Fri Aug 8 15:30:18 2014 +0400

    Fix possible NPE during expression extraction refactoring

    It may occur in cases of variable extraction like "(21 + <start>21) and 'spam'<end>".