commit d519a8ea8182b236a9da17e974726f9c9d54f301
Author: adammurdoch <a@rubygrapefruit.net>
Date:   Wed Jun 9 12:33:20 2010 +1000

    Some improvements to error reporting when something goes wrong with executing test process:
    - WorkerProcess.start() blocks until process has been started and has connected back to the parent process
    - Split up and simplified some of the remote dispatch classes, and added some unit tests