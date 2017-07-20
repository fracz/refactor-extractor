commit 902102f94eeb15da71d4baf8fcdc8ac45c273e15
Author: Jose Lorenzo Rodriguez <jose.zap@gmail.com>
Date:   Mon Dec 8 23:41:57 2014 +0100

    Hadling ResultSet::first() trhough collection functions

    In the past we had to handle the first() function differently so we
    could close the statement. Doe to improvemets in how the statement
    is closed in te query and the connection, this is no loger required.