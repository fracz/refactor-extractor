commit 5ed52b98a1e1b7d0f09949274f454d2de513f496
Author: Carsten Brandt <mail@cebe.cc>
Date:   Sat Jun 15 14:10:47 2013 +0200

    renamed Application::handle() to handelRequest()

    Name make more sense imo as it is not clear what it handles otherwise.
    Removed unused method processRequest form console app and also refactored
    console Application to be more similar to web Application.