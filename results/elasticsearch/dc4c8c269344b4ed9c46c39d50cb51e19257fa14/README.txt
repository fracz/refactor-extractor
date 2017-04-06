commit dc4c8c269344b4ed9c46c39d50cb51e19257fa14
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Mon Mar 16 12:14:33 2015 -0700

    Tests: improve logging of external node version and build

    The BWC tests also run against a snapshot build of previous release branches. Upon a failure it's important to know what commit exactly was used.

    Closes #10111