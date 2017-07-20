commit bda355b5b11f7af081c66e388f0913d84ce5cdce
Author: phpnut <phpnut@cakephp.org>
Date:   Fri May 18 17:19:53 2007 +0000

    Merging Fixes into the trunk:

    Revision: [5110]
     * Adding sequence map to DboPostgres for database tables using non-standard sequences

    Revision: [5109]
     * Adding tests for 'page' and 'offset' with hasMany bindings

    Revision: [5108]
     * Adding test that fails when using page + limit and offset + limit on a HABTM binding

    Revision: [5107]
     * updating scaffold links and views

    Revision: [5106]
     * updating db config task

    Revision: [5105]
     * Applying patch from #2575 adds summary attribute to SQL query table in debug mode

    Revision: [5104]
     * Adding media as an uninflected word

    Revision: [5103]
     * updating project task

    Revision: [5102]
     * updating project task

    Revision: [5101]
     * updating project task

    Revision: [5100]
     * fix to paths in shell

    Revision: [5099]
     * updating shell paths and help

    Revision: [5098]
     * Changing order of XML class defs (Ticket #2572)

    Revision: [5097]
     * Adding missing returns

    Revision: [5096]
     * updating acl shell

    Revision: [5095]
     * adding bake tasks

    Revision: [5094]
     * updated file class, console classes

    Revision: [5093]
     * Fixing support for error messages when multiple validation rules are used with numeric indexes

    Revision: [5092]
     * updated folder class and test

    Revision: [5091]
     * Adding fix for errors on some linux and windows install when using bash scripts

    Revision: [5090]
     * Removing unused tasks

    Revision: [5089]
     * Fixing incorrect index in ACL shell script error message, and clarifying help text

    Revision: [5087]
     * Adding tests and fixes for #2570, this corrects DboSource::conditions fails to quote mixed array/string conditions

    Revision: [5086]
     * Correcting doc comments

    Revision: [5085]
     * Adding schema support to Postgres driver (Ticket #1623)

    Revision: [5084]
     * refactoring paths

    Revision: [5083]
     * Fixing output caused when class was loaded in console

    Revision: [5082]
     * Refactoring translation string extractor class to work with new console

    Revision: [5081]
     * updating baking of models

    Revision: [5080]
     * fixing paths in shell

    Revision: [5079]
     * fixing error in shell taskNames

    Revision: [5078]
     * Fixing cache setting when installing a fresh version on a Win box

    Revision: [5077]
     * updating tasks, removing BakeTask base class, Task extends Shell

    Revision: [5076]
     * updating shell task handling, removing bake2

    Revision: [5075]
     * updating acl and bake shells

    Revision: [5074]
     * adding tasks and uses to shells, var $tasks and var $uses. if the task exists in the var it will be executed if the command is the same as the task. uses will load the model.

    Revision: [5073]
     * removing em from css

    Revision: [5072]
     * Merging [5070] into DboMysqli and also adding changes to 1.1.x.x versions

    Revision: [5071]
     * Fixing and adding tests for ticket #2551

    Revision: [5070]
     * Fixing problem in listSources when using '-' in database name

    Revision: [5069]
     * Adding admin route matching fix (Ticket #2541)

    Revision: [5068]
     * updating shell

    Revision: [5067]
     * updating model constructor in 1.1, 1.2 to use the $table param

    Revision: [5065]
     * Adding fix for irregular inflections returning word in lowercase

    Revision: [5064]
     * updating bake setCake

    Revision: [5063]
     * updating paths for shell, bake

    Revision: [5062]
     * Fixing ACL script for new model interface (Ticket #2164)

    Revision: [5061]
     * updating paths for shell

    Revision: [5060]
     * Adding help() as a default command if main() is not defined in shell script

    Revision: [5059]
     * updating paths for shell

    Revision: [5058]
     * updating shells for new conventions, better handling of paths, more help

    Revision: [5057]
     * Adding directories to skel/vendors

    Revision: [5056]
     * Renaming file

    Revision: [5055]
     * Renaming vendors/console to vendors/shells

    Revision: [5054]
     * not fully complete changes but adding so we can change from Script to Shell

    Revision: [5053]
     * adding application specific vendors/console/templates

    Revision: [5052]
     * Adding templates directory to vendors/console to allow overriding templates used by the cli

    Revision: [5051]
     * Removing unneeded empty files

    Revision: [5050]
     * Adding application specific directories to vendors

    Revision: [5049]
     * moving tasks directory

    Revision: [5048]
     * Adding console directory to vendors.
     * Added missing empty files

    Revision: [5047]
     * Added fix for #2545, fixes VALID_NUMBER does not allow negative values

    Revision: [5046]
     * Adding fix for #2552, fixes JavascriptHelper::object does not convert boolean values correctly

    Revision: [5045]
     * Adding fix for #2538, fixes findAll doesn't work with TranslateBehavior when specifying which fields to fetch

    Revision: [5044]
     * Adding test for #2550.
     * Added fix for #2550, fixes incorrect inflection of Alias

    Revision: [5043]
     * Adding fix for #2534, refactored mysql and mysqli dbo

    Revision: [5042]
     * Added fox for #1815

    Revision: [5041]
     * Adding fix for #2547, fixes issue when saving arrays with numeric keys

    Revision: [5040]
     * Applying patch of ticket #2560

    Revision: [5039]
     * Removing unused function

    Revision: [5038]
     * updating scaffolding and css

    Revision: [5037]
     * Correcting call to __value and using dot notation in button()

    Revision: [5036]
     * Updating help text on ACL script

    Revision: [5035]
     * Applying patch from ticket #2540

    Revision: [5034]
     * Removing bake() function and implementation of help()

    Revision: [5033]
     * Fixing undefined variable error

    Revision: [5032]
     * Adding fix for #2530, adds support for Oracle to acl cli

    Revision: [5031]
     * Fixing paths erros in new bash and CLI scripts.
     * Updated missing files and directories in skel.
     * Corrected paths for app custom scaffold templates

    Revision: [5030]
     * Adding empty file to all empty directories

    Revision: [5029]
     * Copying missing directories to skel/views/layouts

    Revision: [5028]
     * Copying i18n.sql to skel directory

    Revision: [5027]
     * Adding sanity checking / warning messages for data returned from Model::loadInfo() (thanks phishy)

    Revision: [5026]
     * Reverting last commit

    Revision: [5025]
     * Renaming file to see if executable bit will remain

    Revision: [5024]
     * Test commit to see if executable bit is removed

    Revision: [5022]
     * Correcting property settings on file

    Revision: [5021]
     * Correcting property settings on file

    Revision: [5020]
     * Correcting line endings on bash script.
     * Starting to correct paths to new console directory.
     * Replaced SCRIPTS define with CONSOLE_LIBS

    Revision: [5019]
     * Adding possibility to specify if option values should be escaped, and adding tests for it

    Revision: [5018]
     * Removing wrong delete call

    Revision: [5017]
     * Moving dispatch.php to console/cake.php

    Revision: [5016]
     * updating acl console script

    Revision: [5015]
     * Restructuring console directory

    Revision: [5014]
     * Adding new libs directory

    Revision: [5013]
     * Renaming scripts directory to better explain what it is for

    Revision: [5012]
     * Removing the _task from file names

    Revision: [5011]
     * updating scripts and skel homepage

    Revision: [5010]
     * Adding patch from #2528, fixes validation passing when even when allowEmpty === false

    Revision: [5009]
     * converted bake script to use console. refactored some other script code

    Revision: [5006]
     * adding commands to scripts,  similar to controller actions, will call main action if it exists, cleaning up some other stuff

    Revision: [5005]
     * fixing typo in ConsoleScript

    Revision: [5004]
     * Fixed folder test

    Revision: [5003]
     * Adding tests for Folder

    Revision: [5002]
     * fixed missing dispatcher in ErrorHandler, updated AclScript, added initialize method to CakeScript which should be used by subclasses as the constructor.

    Revision: [5001]
     * Adding empty test cases for File and Folder

    Revision: [5000]
     * updating createFile in CakeScript, adding mode to Folder construct in File class

    Revision: [4999]
     * changing reference to createFile in acl.php

    Revision: [4998]
     * updating console scripts, added script path searching, for /vendors and /app/vendors, class names must be like "AclScript" in /scripts/acl.php, all scripts now extend CakeScript

    Revision: [4997]
     * Renaming file to cake_script.php

    Revision: [4996]
     * updating console_script

    Revision: [4995]
     * Adding fix for #2522, fixes errors when primary key is not auto increment and the primary key is set in the data to be saved.

    Revision: [4994]
     * Cleaning up line endings

    Revision: [4993]
     * updating default homepage

    Revision: [4992]
     * Updating ACL script for new Console classes

    Revision: [4991]
     * Adding fix for #2512, fixes translation behavior count queries

    Revision: [4990]
     * Adding newlines to ConsoleScript::hr()

    Revision: [4989]
     * Adding tests for #2520

    Revision: [4988]
     * Renaming file extensions of scaffolding templates to ctp

    Revision: [4987]
     * Fixing problem with submit options in FormHelper::end() (ticket #2516)

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@5118 3807eeeb-6ff5-0310-8944-8be069107fe0