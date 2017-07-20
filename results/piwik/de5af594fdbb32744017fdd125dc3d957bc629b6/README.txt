commit de5af594fdbb32744017fdd125dc3d957bc629b6
Author: diosmosis <benaka.moorthi@gmail.com>
Date:   Mon Mar 11 00:58:21 2013 +0000

    Refs #2908, refactored tests so database setup (adding sites, tracking visits) are separated from API tests. Put setup code into fixtures and reused code as much as possible.