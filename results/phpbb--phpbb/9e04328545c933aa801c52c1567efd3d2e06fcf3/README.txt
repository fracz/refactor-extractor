commit 9e04328545c933aa801c52c1567efd3d2e06fcf3
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Tue Jun 25 14:24:47 2013 -0400

    [feature/auth-refactor] Test autologin() on provider_apache

    Provides a test for the autologin() method of provider_apache
    that assumes the user already exists in the database.

    PHPBB3-9734