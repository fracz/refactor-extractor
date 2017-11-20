commit 3a1ab28df7befdac5a2331c35e0fd817f8c9d313
Author: Damien CORNEAU <corneadoug@gmail.com>
Date:   Mon Aug 8 19:35:25 2016 +0900

    [ZEPPELIN-1290] Refactor Navbar Controller

    ### What is this PR for?
    This is a small refactoring to keep this Controller following the [ControllerAs with vm](https://github.com/johnpapa/angular-styleguide/tree/master/a1#controlleras-with-vm)
    rules, that it was based on.

    Here is a list of things that were changed and why:

    * Most of the controller's $scope values & fct (except from the search q) where moved to the vm.The controller is using vm, so storing in $scope to share with the view is not needed.

    * Functions or Vars that are not used in the view were removed from the vm. (kept private to the controller)

    * $rootscope functions was moved to `app.js`. I think  the need for that function might need to be changed, but for the scope of this PR, we are just moving it to where the $rootScope values are declared.

    * Gathering vm declaration before the functions and ordered alphabetically

    * Re-order functions alphabetically

    * Create `initController ` to regroup all the controller setup.

    ### What type of PR is it?
    Refactoring

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-1290

    ### How should this be tested?
    You can Just verify that the below Navbar related features are still good:
    * Search Form
    * Connected Status
    * Login button
    * User Name and its dropdown menu
    * Notebook list drop-down menu (and filer, folder inside of it)

    ### Questions:
    * Does the licenses files need update? No
    * Is there breaking changes for older versions? No
    * Does this needs documentation? No

    Author: Damien CORNEAU <corneadoug@gmail.com>
    Author: CORNEAU Damien <corneadoug@gmail.com>
    Author: Prabhjyot Singh <prabhjyotsingh@gmail.com>

    Closes #1281 from corneadoug/Refactor/navbarCtrl and squashes the following commits:

    31f9110 [CORNEAU Damien] Merge pull request #4 from prabhjyotsingh/logoutUserFix
    4686a18 [Prabhjyot Singh] CI failure for testGroupPermission
    2fde749 [Damien CORNEAU] finish cleaning the controller
    be18547 [Damien CORNEAU] Remove  functions from navbar controller