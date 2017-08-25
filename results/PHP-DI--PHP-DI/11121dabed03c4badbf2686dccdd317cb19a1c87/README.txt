commit 11121dabed03c4badbf2686dccdd317cb19a1c87
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Tue May 24 19:39:13 2016 +0200

    Remove definition dumpers: definitions can now cast to string directly

    That allows to remove 9 classes (plus 8 test classes) and a lot of code that was not very useful at all. It has been refactored into something much simpler: definitions can be simply cast to string.