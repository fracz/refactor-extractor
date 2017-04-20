commit 10dab495f296fcef5a8b6afb380e91c57c134d59
Author: Tim Yung <yungsters@fb.com>
Date:   Wed Jul 10 14:45:11 2013 -0700

    Stop Unnecessary Purging of Node Cache

    When each component unmounts, it already cleans up its respective entry in the node cache. Let's stop blowing away the entire node cache unnecessarily.

    This should improve performance because a React component's root will never need to be searched for more than once.