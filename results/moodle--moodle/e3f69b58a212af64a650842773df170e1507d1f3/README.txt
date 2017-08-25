commit e3f69b58a212af64a650842773df170e1507d1f3
Author: spvickers <github@spvickers.freeserve.co.uk>
Date:   Wed Sep 3 23:35:39 2014 +0100

    MDL-45843 mod_lti: introduced support to LTI 2.0

    This commit introduces support to the LTI module for LTI 2.0.
    As well as the initial commit the following changes were made
    and then squashed into the single commit for integration:

    * Fixed bug in services
      Fixed bug which limited characters allowed in values of template
      variables (e.g. vendor ID) in service endpoints.
      Changed language file to refer to tool registrations rather than
      tool proxies.

    * Refactored service classes
      Moved classes relating to services into areas where Moodle will
      autoload them

    * Ran code through code checker
      Removed all errors reported by the Code checker module
      excluding third-party OAuth.php file.

    * UI improvements
      Mainly when adding an external tool to a course - fields which
      should not be changed for a selected tool are either hidden or
      disabled. Admin settings page now shows the Tool Registration
      name against a tool rather than the launch URL, and the
      registration URL replaces the GUID on the tool registrations
      page.

    * Updated tool proxy registration
      Added check of tool proxy to ensure only offered capabilities
      and services are included.  Also check tool proxy when processing
      a service request.

    * Code review changes
      Some fixes based on code review by Mark Nielsen and addition of
      some PHPDocs comments.

    * Updates from code/PHPdocs checks
      Removed use of eval and corrected invalid PHPdocs for new
      functions/classes

    * Corrected namespace error and incorrect string terminator

    * Updates based on forum feedback
      Added dependencies and backup, restore and uninstall methods for
      ltiservice subplugins.
      Changed most uses of is_null to empty

    * Updated custom parameters test
      Updated test_split_custom_parameters to include new function
      parameters.
      Corrected PHPdoc entry for lti_split_custom_parameters
      Fixed incorrect line separators in ltiservice.php

    * Added require_capability to registrationreturn.php

    * SQL and EOL updates
      Moved PHP variable in SQL into a named parameter
      Improved checks for end-of-line characters to include CR and LF
      on their own or together

    * Check for semicolon separators
      Semicolon separators in custom parameters are changed to EOL
      characters when upgrading to the 2014100100 version.

    * Remove unused file
      basiclti.js file not being used so removed.

    * Adjust line lengths
      Split long lines in upgrade.php

    * Added savepoint to upgrade.php
      savepoint omitted from earlier update to upgrade.php

    * Updated namespaces and upgrade
      Service and resource classes moved into .../local/...
      Upgrade SQL moved into a function and unit test created

    * Updated lti_tool_proxies table
      Added indices and foreign keys to lti_tool_proxies table

    * Fixed formatting and documentation issues

    * ltiservice class moved into local

    * Replaced lti_scale_used comments
      Put back commented out code for lti_scale_used

    * Removed redundant sesskey code

    * Fixed namespace and path check
      Updated ltiservice namespace for move into local
      Added check for existence of $_SERVER['PATH_INFO']

    * Updated upgrade code
      Added indices and keys to lti_tool_settings table when upgrading
      Fixed errors in upgradelib_test.php (thanks to jleyva)
      Update SQL to use Moodle functions

    * Use of empty with class method
      PHP 5.4 does not like the use of empty with a class method so saved the
      value to a variable first.  PHP 5.5 seems to accept the use of a method
      with empty.

    * Removed redundant indices
      Removed creation of indices for foreign keys on lti_tool_settings table
      from install.xml and upgrade.php

    * Fixes based on feedback
      Minor changes and corrections based on review in JIRA

    * Fixed bug in toolproxy service
      Corrected bug which failed to respond properly to an invalid request
      Also updated upgrade.txt file

    * Improved admin navigation
      Added the manage tool registrations page as a separate entry on the
      admin menu (within a folder named LTI).  Made this entry the current
      position for the related pages.

    * Updated PHPdocs with class names
      Added class names with namespaces to PHPdocs to replace generic
      references to "object"

    * Changed object to iframe
      Use of object tag in register.php changed to use an iframe tag in line
      with the similar update made to view.php.

    * Improved registration process
      A message is now displayed if the registration page has not been loaded
      in the iframe within 20 seconds.  If a user is returned to Moodle
      without a tool proxy being sent, the registration is moved back from
      pending to configured.

    * Fixes for integration
      Removed comment - the template is the default path unless overridden, so
      get_path and get_template should both be defined.
      Added comment and intval to fix the issue with obtaining an error
      reason.