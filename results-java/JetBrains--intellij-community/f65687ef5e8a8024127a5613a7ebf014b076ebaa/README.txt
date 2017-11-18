commit f65687ef5e8a8024127a5613a7ebf014b076ebaa
Author: irengrig <irina.chernushina@jetbrains.com>
Date:   Wed May 11 14:33:45 2016 +0200

    Node.JS Docker remote execution: in case of docker debug, change host that debugger listens to, to docker machine ip, also do not change debugger port to reported by docker local debug port
    - debugging works now!
    refactor other node-docker code for clarity, create only 1 supporting image per project, cache its id in workspace.xml, keep Dockerfile for image under project
    (cherry picked from commit d026324)