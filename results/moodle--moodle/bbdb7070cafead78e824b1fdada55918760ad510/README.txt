commit bbdb7070cafead78e824b1fdada55918760ad510
Author: tjhunt <tjhunt>
Date:   Tue Nov 18 07:10:00 2008 +0000

    manage roles: MDL-8313 Provide a basic mode for the manage roles page.

    * New basic define roles mode, with just an Allow checkbox for each capability.
    * Button to toggle this form to/from advanced mode.
    * Also, a separate mode for viewing a role definition, rather than just showing disabled checkboxes.
    * Now duplicating a role just takes to you a pre-populated add role form, so you can double-check things before saving the new role.
    * Deleting a role is now logged.
    * Role reordering code cleaned up.
    * You can now no longer delete the last role that has admin permissions.
    * This includes a general refactor of manage.php, which eliminates manage.html, and splits of define.php.