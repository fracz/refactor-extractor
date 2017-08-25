commit fcf46da1c5e51fce71ed9c7525ca6307c93d6532
Author: Inaki <iarenuno@eteo.mondragon.edu>
Date:   Sun Jul 25 22:36:15 2010 +0000

    auth/ldap cas/ldap MDL-23371 auth/ldap and auth/cas refactor

    They now share most of the code again, this time via subclassing, and they
    share some code with enrol/ldap. They have also gained some features and a few
    fixes.