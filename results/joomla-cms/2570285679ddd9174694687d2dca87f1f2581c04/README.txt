commit 2570285679ddd9174694687d2dca87f1f2581c04
Author: Allon Moritz <allon.moritz@digital-peak.com>
Date:   Sat Oct 29 15:42:32 2016 +0200

    Custom fields (#11833)

    * Update gallery.xml

    * Update fields.php

    * Update internal.php

    * Update field.php

    * Update dprules.php

    * Update section.php

    * Update base.php

    * Update view.html.php

    * Update view.html.php

    * Update render.php

    * Update modal_article.php

    * Update sql.php

    * Update fields.php

    * Remove commented debug code

    * Update fields.php

    * #14 fixed Joomla Coding Standards and added function dock blocks code

    * #14 fixed Joomla Conding Standards and added function dock blocks

    * #14 Fixed class dock block mistake

    * we did not require this any more

    * #14 fixed Joomla Coding Standards and added function dock block in helper file at back-end

    * #14 Fixed only Joomla Coding Standards in FieldsHelper file

    * #14 removed where function, added dock blocks and fixed dock bocks coding standards errors

    * #14 formatted as the function doc blocks

    * #14 formatted as the function doc blocks

    * Check the active menu correct

    Closes #72

    * Move the label field to the options tab

    Closes #22

    * Label has it's own field and must not stay in params

    Regression from #22

    * Backport from Digital-Peak/DPFields#32

    * Fix the save and copy action

    Closes #37

    * Store data on reload

    Closes #76

    * Don't add always the All language to the query

    Closes #38

    * Count the items in the category manager from com_fields

    Closes #61

    * Corrected colspan when fields are assigned to a language

    * #14 fixed conflict

    * Don't save the All category

    Closes #19

    * Minor language updates

    * Fix "Show On" tooltip's description

    The tooltip's description for the 'Show On' field is not showing because it is currently looking for the label.

    * Don't hardcode com_content.article

    Closes #80

    * Load the types form when the data is an array

    * Create the links correct for the count item feature

    Closes #82

    * Show the value from com_fields instead of the the users helper

    Closes #81

    * Set the language correct on the fields cache

    Closes #47

    * Removed string conversion error

    Closes #47

    * Changed links to 3.7

    * Remove legacy DPFields code

    * Get data from state correctly

    * Add joins to the com_fields in search plugin (#83)

    Add joins to com_fields in the content search plugin

    * Add new core rule core.edit.value (#75)

    * Add 'onFieldBeforePrepare' and 'onFieldAfterPrepare' events

    * Rename "Field Category" to "Field Group"

    Closes #91

    * Add option to disable custom fields per component

    Closes #87

    * Use select box to define the images directory

    Closes #97

    * Truncate correctly

    Closes #101

    * Change description field to plain textarea

    Closes #94
    CLoses #99

    * Parameters are more clear and improved comment

    Adapted change from
    https://github.com/joomla/joomla-cms/pull/10722/commits/32c9048b88383ba6028743a48517176952d9a574

    * onFieldBeforePrepare and onFieldAfterPrepare events now triggered even when custom field value is empty

    * Cast before access

    * Create params even when they are empty

    * Add com_fields menu entries on back end

    Closes #109

    * Extract the context correctly

    Closes #112

    * Add custom fields for contact

    * Add inline group create

    Closes #114

    * Add simple context mapping

    Closes #117

    * Dont assign a not existing category when a field is saved with no catid

    * Rename string with group and not category

    Closes #124

    * Category strings should be named Field Groups

    Closes #126

    * Show the fields on the blog listing

    Closes #108

    * Installer crashes somehow

    Closes #132

    * Merge the types set up into JFormField (#104)

    * Merge field types into JFormFields

    * Converting the rest of the fields

    * Merge DP Field Rendering with Joomla Form Fields

    * Remove the gallery scripts

    * Rename the interface to JFormDomfieldinterface for auto loading

    * Add article field as demo how a component can add fields

    * Removing mustache, will be replaced later with a new parser

    See #12 for the discussion

    * Fixing composer setup, reverting to 3.6.x branch

    * Fixing code style errors

    Closes #14

    * Rename upgrade file to 3.7

    * Strip slashes from description

    Related to #139

    * Removing Simplepie from the database installer script

    Reverts 8381e46758d18c6286fde35c52b1a2a475d7d0df
    See comments for more information

    * Show field group description in form

    Closes #139

    * List field changed key to value and value to name

    Closes #121

    * Use the first category as field filter when creating an article

    Closes #144

    * Dont show a user field on the front end

    Closes #149

    * Add always the fields path when adding the custom fields

    Closes #150

    * Added hint (placeholder) option

    Closes #151

    * Correct redirect after checkin action

    Closes #154

    * Check for group access levels when getting the fields (#153)

    * Consider field group state

    Closes #155

    * Added missing language strings

    Closes #129

    * JArrayHelper => ArrayHelper

    * reduce model method (#160)

    * Fixing permission inheritance to edit the value of a field in the form (#157)

    * Correct path for parameters folder when not in Joomla libraries

    * Type selection (#162)

    Closes #134

    * Display class attributes (#143)

    * User home (#161)

    * Add home parameter to media field

    * Correct path for paramneters folder when not in Joomla libraries

    * Fixing code style errors

    * Add new context for com_contact for contact form fields (#115)

    * Add new context for com_contact for contact form fields

    * Prepare the value on com_contact mail

    * Fixing travis code style errors

    * Use the correct variable name

    Closes #163

    * Show the all label when no category is assigned

    Closes #165

    * Don't set 0 as default hint when not present

    Closes #166

    * Fix Sniffer Whitespace errors

    * Fix Sniffer Whitespace errors (#167)

    * Cleanup field model

    * Support fields plugins

    * Add gallery plugin

    * Moved since tag to 3.7

    * Some brushup

    * Update controller.php

    * Update field.php

    * Update fields.php

    * Update controller.php

    * Update fields.php

    * Update internal.php

    * Update fields.php

    * Update fields.xml

    * Update field.php

    * Update fields.php

    * Update type.php

    * Update section.php

    * Update field.php

    * Update field.xml

    * Update filter_fields.xml

    * Update field.php

    * Update view.html.php

    * Update edit.php

    * Update modal_options.php

    * Update view.html.php

    * Update default.php

    * Update default_batch_body.php

    * Update modal.php

    * Update field.xml

    * Update en-GB.com_fields.sys.ini

    * Update en-GB.com_fields.ini

    * Update en-GB.plg_fields_gallery.ini

    * Update en-GB.plg_fields_gallery.sys.ini

    * Update en-GB.plg_system_fields.ini

    * Update en-GB.plg_system_fields.sys.ini

    * Update modal_article.php

    * Update controller.php

    * Update field.php

    * Update default_custom.php

    * Update captcha.php

    * Update editor.php

    * Update media.php

    * Update user.php

    * Update editor.xml

    * Update media.xml

    * Update user.xml

    * Update usergrouplist.xml

    * Update abstractlist.php

    * Update domfieldinterface.php

    * Update field.php

    * Update email.php

    * Update file.php

    * Update imagelist.php

    * Update sql.php

    * Update tel.php

    * Update textarea.php

    * Update url.php

    * Update calendar.xml

    * Update checkboxes.xml

    * Update imagelist.xml

    * Update integer.xml

    * Update list.xml

    * Update radio.xml

    * Update sql.xml

    * Update textarea.xml

    * Update url.xml

    * Update gallery.xml

    * plugin

    * plugin2

    * typo

    * Change to uft8mb4

    * Add new extensions to the script file

    * Revert merge conflict composer changes

    * No needed changes

    * Library form fields should be unaware of com_fields

    * Default to https

    * Order the tables correct

    * Cleanup language strings

    * Code errors from cleanup

    * Remove tags support

    * Add category filter

    * Display fielg group label when editing a group

    * Display all fields which are assigned to all languages correct

    * Don't show fields on categories as it is not ready yet

    * Update fields.php

    * Don't transofrm the category name

    * Correct quote name function used

    * Revert "Display fielg group label when editing a group"

    This reverts commit 133133267e88a341cb5634d814a87e06f3e8daf3.

    * Disable tags on custom field groups

    * Don't include category state in filter on back end

    * Remove debug code

    * CS fix

    * Shortening the line length

    * Install SQL files for postgres

    * Use JDatabaseQuery

    * Option to show the user custom fields on the front on the contact view

    * Comment for item_id table column

    * Remove class for textareas

    * Fix no menu item pages giving wrong links (#12020)

    * [plg_system_logout] Load language files only when needed (#11736)

    * Update logout.php

    * cs and stuff

    * Update logout.php

    * Adding Options Button (#12033)

    * fix paths (#12032)

    * Fix routing for non-sef menu items with Modern Routing (#12021)

    * [com_banners] - publishing time does not honor timezone (#11978)

    * [com_banners ] -  publishing time does not honor timezone

    * impress only when needed

    impress only when needed

    * CS fix

    cs fix

    * Order stylesheet attributes the same as link attributes (#12052)

    * use joomla-projects docker

    * Phase out JString calls in libraries (#12058)

    * Admin app - JString -> StringHelper (#12056)

    * Site app & Plugins - JString -> StringHelper (#12057)

    * New Feature, Multilanguage: Add the possibility of displaying associations in article info and in articles list. Replaces #11935 (#12042)

    * Replaces

    * Modifying to fit Thomas proposal

    * modifying fetching flags param

    * adding showon + some

    * [JAccess] Improve ACL asset preloading performance/memory consumption (#12028)

    * improve component asset preload speed

    * Update user.php

    * Update access.php

    * Update access.php

    * Update access.php

    * Update access.php

    * not convinced yet, but revert changes in juser

    * make sure that components are always preloaded

    * only a logged users (with user id) can be root.

    * [com_contact] - moved the captchaEnabled var from default to view (#11964)

    * [com_contact] - moved the captchaEnabled var out of the default.php file to the view.html.php file

    moved the captchaEnabled var out of the default.php file to the view.html.php file.

    * moved captchaEnabled to the view

    moved captchaEnabled to the view

    * missed var  declarationi/initialization

    missed var   $captchaSet  declarationi/initialization

    * Session interaction refactoring (#10905)

    * Allow lazy starting a session

    * Defer starting sessions in the installer

    * Defer starting session in JApplicationCms

    * Deprecate creating sessions in JFactory

    * Start restructuring session load sequence in JApplicationCms

    * Add JSession object to onAfterSessionStart event trigger

    * Override afterSessionStart for install app to not store the user

    * Expand the database storage object for better API

    * Fix session listener declaration

    * Restructure session bootup and when the session metadata operations run

    * 3.5 -> 3.6

    * Remove option, out of scope

    * Tweak things so the behavior is closer to current API

    * Undefined variable

    * Get store name from session object

    * Remove this, out of scope

    * PHPCS fix

    * Deep rooted application dependencies are my absolute favorite

    * Move comment, doc blocks, consistent method signature

    * Again on the consistency, we don't like E_STRICT errors

    * Nevermind, confused myself with checkSession, carry on...

    * Until the day the unit tests no longer follow the doc block code styles...

    * Lost line in merge

    * One more merge conflict fix

    * This internal coupling is addressed

    * Fix query from merge

    * [com_content] - add articles ordering by votes, ratings (#11225)

    * Add setDocumentTitle() function in JViewLegacy (#11399)

    * setDocumentTitle() function in JViewLegacy

    * PHPCS

    * JED turned to https (#12076)

    * JED truned to https

    JED truned to https
    Minor fixes on proper use of Joomla!

    * Update README.txt

    Same as #12076

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED turned https

    * JED and community turned https

    * JED, community and org turned https

    * JED and community turned https

    * JED and community turned https

    * correct/add quotename (#12070)

    * Label: Users Options: Login name > Username (#12073)

    The language label "Change Login Name" is not consistent with the front-end label "Username".

    This PR changes the language label to "Change Username" and the decription to "Allow users to change their Username when editing their profile."

    The front-end login screens use the label "Username"
    Login Component:
    ![login-component](https://cloud.githubusercontent.com/assets/1217850/18627996/579510ca-7e5e-11e6-9161-6268e9ccb213.png)

    Login Module:
    ![login-module](https://cloud.githubusercontent.com/assets/1217850/18627997/579595ae-7e5e-11e6-8920-edf34631a3c7.png)

    ### Testing Instructions
    #### Before the PR
    Users > Manage > [Options] > [User Options]
    The last entry is "Change Login Name"
    + hover description "Allow users to change their Login name when editing their profile."

    ![login-back-end-options](https://cloud.githubusercontent.com/assets/1217850/18627995/5791ac32-7e5e-11e6-8b18-94a34372c422.png)


    #### After the PR
    Users > Manage > [Options] > [User Options]
    The last entry is "Change Username"
    + hover description "Allow users to change their Username when editing their profile."

    ![login-back-end-options-after](https://cloud.githubusercontent.com/assets/1217850/18627998/5799ceb2-7e5e-11e6-9a5e-234dcf2db14e.png)

    * Update .travis.yml (#12079)

    * Add vote order check

    * [a11y] Protostar back to top (#12446)

    * [a11y] Protostar - back to top link

    * Oops Andre was right

    * add anchor for non-js enabled browsers

    * Added missing com_fields component from last merge