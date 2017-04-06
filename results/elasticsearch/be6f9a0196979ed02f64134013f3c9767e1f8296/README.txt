commit be6f9a0196979ed02f64134013f3c9767e1f8296
Author: Robert Muir <rmuir@apache.org>
Date:   Mon Aug 3 14:43:40 2015 -0400

    improve sanity of securitymanager file permissions

    conf/ and plugins/ do not need read-write access: this prevents a lot
    of bad possibilities.