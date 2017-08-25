commit e059c033d9fa317753b28f88b2290c791eb663c3
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Fri Oct 5 11:59:00 2012 +0100

    MDL-35802 enrol other users: should use Show user identity setting.

    As part of fixing this, I refactored some common code out of
    get_potential_users and search_other_users. Previously, only one of
    those bits of code had been updated.