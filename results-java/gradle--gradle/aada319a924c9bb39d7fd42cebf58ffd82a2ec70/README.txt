commit aada319a924c9bb39d7fd42cebf58ffd82a2ec70
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Apr 20 21:03:20 2012 +0200

    Improvements around daemon support in ExecHandle spec.

    -Made the ExecHandle interface more natural wrt daemon support: we start() the process and we have an option to detach() from it.
    -It is possible now to abort() detached process.
    -Moved more responsibility to the StreamsForwarder - he owns his executor now and provides a stop() method to the clients. This feels more natural and cleaner.

    There're few more things that can be improved in the area.