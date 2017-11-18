commit d7b0cca0b3f69dff194e537e695c26e7daa92cee
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Sep 9 10:16:09 2011 +0200

    Some refactorings in the client so that the code better describe what really happens. Added specific handling for stop and build commands because they need specific handling anyways (e.g. for exception handling, etc.). Made the stop command more solid - we ignore when the server does not return a message on assumption it is already stopped.