commit e141ab680db2b9ec7839eab30612c4423f61bf00
Author: Nate Abele <nate.abele@gmail.com>
Date:   Tue Feb 23 07:03:07 2010 -0500

    Merging "plugin" and "library" concepts; both are now handled uniformly as "libraries". Removing `app/libraries/plugins` directory, removing plugin loading / configuration code, and refactoring `\core\Libraries::add()`. Adding route auto-loading to filter in `app/config/bootstrap/action.php`.