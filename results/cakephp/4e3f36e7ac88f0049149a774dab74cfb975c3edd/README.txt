commit 4e3f36e7ac88f0049149a774dab74cfb975c3edd
Author: phpnut <phpnut@cakephp.org>
Date:   Fri Feb 2 06:53:25 2007 +0000

    Merging fixes into the trunk:

    Revision: 4404
    Adding fix for #2012

    Revision: [4403]
    Adding fix for #2021

    Revision: [4402]
    Changing the order of include_path this is done to fix issues with servers having core cake files in one location and a user wanting to use a local copy

    Revision: [4401]
    Added fix for #1991

    Revision: [4400]
    Adding redirect fix for Ticket #1951

    Revision: [4399]
    Disabling REST-compliant hidden field temporarily

    Revision: [4397]
    Applying patch from #2019

    Revision: [4396]
    Adding magic method fix for null query values (Ticket #1999), and adding PostgreSQL boolean quoting patch (Ticket #1788)

    Revision: [4395]
    adding patch to dbo_postgres, #1828

    Revision: [4394]
    Adding patch from #1844

    Revision: [4393]
    Adding fix for #1928

    Revision: [4392]
    Added plugin view caching #1805.
    Also fixed #1315

    Revision: [4391]
    Adding patch from #1851.
    Fixed #1851 and #1876

    Revision: [4390]
    Fixed bad merge on Model::bindModel().

    Revision: [4389]
    Adding fix for #1946.

    Revision: [4388]
    fix in acl.php cli for delete and getPath, #1831

    Revision: [4387]
    Updating Japanese translations (ticket #2030)

    Revision: [4386]
    Fix for #2031

    Revision: [4385]
    Adding fix for #1988

    Revision: [4384]
    Adding fix for #2001

    Revision: [4383]
    Adding fix for #2027

    Revision: [4382]
    Adding fix for #1965

    Revision: [4381]
    Adding fix for #2023

    Revision: [4379]
    Adding fix for #1908

    Revision: [4378]
    Adding fix for #1822

    Revision: [4377]
    Adding fix for #1993

    Revision: [4376]
    Correcting doc comments for #1852

    Revision: [4375]
    Reverting changes recent changes to DboSource

    Revision: [4374]
    Fixing undefined variable notice in DboSource::update()

    Revision: [4373]
    Fixing $fields error for belongsTo associations

    Revision: [4371]
    updating inflector rules #1939

    Revision: [4370]
    Adding fix for #1872.
    Fixed helpers not being set in cached files

    Revision: [4369]
    Adding fix for AjaxHelper::drag() (Ticket #1906), and adding AjaxHelper test case

    Revision: [4368]
    Updating DboSource with new query generator implementation, and updating DboSource tests

    Revision: [4367]
    adding fixes to acl cli for #1885, and new upgradedb in acl cli for #1857

    Revision: [4366]
    changing field types to char(2) on table aros_acos

    Revision: [4365]
    Replacing ternary operations in FormHelper, and adding DOM ID's back to hidden inputs

    Revision: [4364]
    Adding fix for #1888

    Revision: [4363]
    adding fixes to bake #1881, #1929, 1891

    Revision: [4362]
    Fixing key quoting in JavascriptHelper::object() (Ticket #1986)

    Revision: [4360]
    Adding PostgreSQL conditional expressions to DboSource (Ticket #1914)

    Revision: [4359]
    Adding fix for #1953

    Revision: [4357]
    Adding insertQueryData fix for foreign keys = 0 (Ticket #1959)

    Revision: [4356]
    Adding fix for #1853

    Revision: [4355]
    Adding patch from #1973
    This fixes issues with singular words that end in us or ss

    Revision: [4353]
    Adding fix for Status singular inflection

    Revision: [4352]
    Allowing user defined inflections to override the core

    Revision: [4350]
    Removing directories that are not used

    Revision: [4349]
    Adding ternary op wrapper function to basics

    Revision: [4348]
    adding uses('set') for Set::normalize() fix in RequestHandler

    Revision: [4347]
    fixing use of normalize in RequestHandler

    Revision: [4346]
    fix for xml helper

    Revision: [4345]
    Adding fix for Notice: Undefined variable: data

    Revision: [4344]
    refactored most methods, transaction support, case insensitive comparison & sorting, added createSequence() and sequenceExists(), bug fixes

    Revision: [4343]
    Applying patch from ticket #2009

    Revision: [4342]
    Enabling join models for all DBOs

    Revision: [4341]
    Updating HtmlHelper::css() to support array paths

    Revision: [4340]
    Fixing magic method query generation for array condition values (Ticket #2008)

    Revision: [4339]
    Updating Model for testing compatiblity

    Revision: [4338]
    Fixing script output caching in JavascriptHelper

    Revision: [4337]
    Rewriting automatic variable detection to work with any field

    Revision: [4336]
    Fixing typo (ticket #2003)

    Revision: [4335]
    Adding Socket core class and unit test

    Revision: [4330]
    Fixing column quoting for array-based conditions when columns are wrapped in SQL functions

    Revision: [4329]
    Fixing column length parsing in DboMysql

    Revision: [4328]
    Fixing error introduced in [4311] the DboSource::limit() does  not return results as expected.
    When column is tinyint(1) is $limit = LIMIT tinyint(1).
    All others are similar result with $limit = LIMIT [column type][#]

    Revision: [4327]
    Refactoring AuthComponent::user()

    Revision: [4326]
    Updating AuthComponent docblocks

    Revision: [4325]
    Fixing AuthComponent redirect URLs

    Revision: [4324]
    Adding shortcut icon generating to HtmlHelper::meta()

    Revision: [4323]
    Adding fallback for deprecated function mysql_list_tables() in DboMysql::listSources()

    Revision: [4322]
    Adding AuthComponent::user() to return current user, fixing config/paths docblocks, and adding IMAGES path

    Revision: [4321]
    Adding automatic password hashing for user logins and creates in AuthComponent

    Revision: [4320]
    Implementing shutdown() callback for components

    Revision: [4319]
    Fixing issue with conditional values being empty arrays in DboSource::conditionKeysToString()

    Revision: [4318]
    Fixing $model parameter in FormHelper::create() (Ticket #1963)

    Revision: [4316]
    Clearing validation errors on Model::create() (Ticket #1960)

    Revision: [4315]
    Updating helper method references, and fixing FormHelper::hidden() to work with '_method'

    Revision: [4314]
    Fixing return value of loadModels()

    Revision: [4313]
    Adding Model::isUnique() for simplified checking of unique fields/values

    Revision: [4312]
    Refactoring AuthComponent, and fixing URL comparison error

    Revision: [4311]
    Adding schema generation support to DboMysql

    Revision: [4309]
    Adding Controller::redirect() fix (Ticket #1951), and fixing redirect URL to conform with HTTP/1.1

    Revision: [4308]
    Adding fix for Ticket #1974

    Revision: [4307]
    Moving normalizeList() (basics) to Set::normalize()

    Revision: [4306]
    Fixing AppController::$uses merging for controllers with no models of their own.  To completely disable $uses, set $uses = null in your controller.

    Revision: [4305]
    Enforcing component callback disabling in Dispatcher (do $this->Component->enabled = false in beforeFilter).

    Revision: [4304]
    Fixing typo in HABTM definition (ticket #1975)

    Revision: [4303]
    Adding some spaces (ticket #1961)

    Revision: [4302]
    fixing FormHelper::input() error messages, ticket #1957

    Revision: [4301]
    Fixing array key name in FormHelper::input()

    Revision: [4300]
    updating bake and css, also added a name key to the generateFieldNames

    Revision: [4299]
    changing cache() to allow writing to the tmp directory

    Revision: [4298]
    Adding 'before', 'between', and 'after' options to FormHelper::input()

    Revision: [4297]
    Adding back 'default' key to table description, and fixing Model::create() to not include empty default values

    Revision: [4296]
    Fixing URL error condition in AuthComponent::startup(), and adding login failure handler

    Revision: [4295]
    Fixing $model parameter for FormHelper::create().  Must pass false if the form is not associated with a model.

    Revision: [4294]
    Adding 'default' option to HtmlHelper::link() to prevent default action

    Revision: [4293]
    Adding FormHelper::input() fix for generating select menus (Ticket #1860)

    Revision: [4292]
    Changed Configure::_ _writeConfig() to use instance from getInstance().

    Revision: [4291]
    Adding check for existence of Core key in class.paths.php.
    Change Configure::_ _writeConfig() to cache files for 10 seconds if DEBUG > 0.

    Revision: [4290]
    Fixing key checking in AuthComponent

    Revision: [4289]
    Adding check for new dot notation that will be used to load classes

    Revision: [4288]
    Adding changes to other load*() functions in basics.php to create cached paths for loaded classes

    Revision: [4287]
    Adding Auth component

    Revision: [4286]
    Reverting changes from [4285]

    Revision: [4285]
    adding a return false to cache and configure

    Revision: [4284]
    Adding addslashes() to Configure::store() so content will have backslashes before characters that need to be quoted.
    Removed error notice that is thrown when file is not present

    Revision: [4283]
    updating cake.generic.css

    Revision: [4282]
    updating FormHelper, #1860

    Revision: [4281]
    fixing typo in bake, #1902

    Revision: [4280]
    updating skel templates

    Revision: [4279]
    adding pagination and sorting to scaffold. refactoring plugin views and redirects, updating default layout

    Revision: [4278]
    adding check for is_writeable to cache()

    Revision: [4277]
    moving data conversion for xml from Model::set() to RequestHandler::startup();

    Revision: [4276]
    adding empty $config to Configure::__writeConfig, update to model for multiple datasources with a table prefix, added option to wrap xml with <data> to wrap the model elements  when using Model::set()

    Revision: [4275]
    Correcting params in doc comments

    Revision: [4274]
    Adding new methods to Configure class that is used to created a cached version of config files.
    Adding Configure::store() method call to bootstrap.php to create a cached file for class paths.
    Adding Configure::read() to load class paths into Configure instance.
    Adding check in loadModel() to get the class path from Configure::read('Models'); if set, if values are not set, the correct file is found and added to the cache file for faster loading.

    Revision: [4273]
    Changing .thtml to .ctp in templates

    Revision: [4272]
    Removing links to wiki

    Revision: [4271]
    Adding Ajax indicator fix for Ticket #1880

    Revision: [4270]
    Adding additional fix to [4269] to bypass cache when multiple forward slashes are used in the url

    Revision: [4269]
    Adding fix to CacheHelper when $this->url is /

    Revision: [4268]
    Fixing $options array merge order in PaginatorHelper::link()

    Revision: [4267]
    Adding 'indicator' feature to AjaxHelper

    Revision: [4266]
    Updating TextHelper::autoLinkEmails() with new API changes (Ticket #1874)

    Revision: [4265]
    Implementing Validation::ip()

    Revision: [4264]
    Implementing minLength and maxLength

    Revision: [4263]
    Fixing typos

    Revision: [4262]
    Adding Sanitize::clean() to replace Sanitize::cleanArray(), cleanValue(), etc.

    Revision: [4261]
    Updating DboPear to be consistent with DboSource API 1.2

    Revision: [4260]
    Adding validation fix for Ticket #1850, and adding warning messages for Model::invalidFields() and Model::validates()

    Revision: [4259]
    Adding FormHelper::create() fix for AjaxHelper (Ticket #1836)

    Revision: [4258]
    Adding PaginatorHelper::options() to set default pagination options

    Revision: [4257]
    Additional helper detection for paginate()

    Revision: [4256]
    Fixing DOM ID's for HtmlHelper::selectTag() (Ticket #1847)

    Revision: [4255]
    Fixing HtmlHelper::link() to allow passing $escapeTitle and $confirmMessage params as 'escape' and 'confirm' in $options

    Revision: [4254]
    Fixing ambiguous key sorting in Controller::paginate()

    Revision: [4253]
    Fixing URL passing in AjaxHelper::form()

    Revision: [4252]
    bake, redirect links

    Revision: [4251]
    bake, generated links

    Revision: [4250]
    re-fixing for the custom controllers in bake

    Revision: [4249]
    updating bake, added fix for ticket #1835 and #1841, css update for #1835.

    Revision: [4248]
    Adding fix to allow Configure::write('debug', '0'); in a controller method

    Revision: [4247]
    Adding Indonesian translation

    Revision: [4246]
    Fixing array key in PaginatorHelper::counter()

    Revision: [4245]
    Cleaning up FormHelper code

    Revision: [4244]
    Adding patch from phishy:

    sets case insensitivity of sorting after connect
    clears statements/cursors when done interating through them
    begun adding transaction support
    fixed date/timestamp support to support more date formats
    added supports for dates and timestamps
    added a sequence public property instead of the hardcoded name

    Revision: [4243]
    Adding skeleton of translation string extractor class

    Revision: [4238]
    making it possible to use FormHelper without a model

    Revision: [4237]
    Adding 'json' parameter for Ajax onComplete callback (Ticket #1843)

    Revision: [4236]
    overfactoring namedArgs, they are used by default with a  colon as a separator

    Revision: [4235]
    Fixing issue in FormHelper::input() where the 'type' attribute appears twice

    Revision: [4234]
    Adding Paginator methods for links and sorting

    Revision: [4233]
    Replacing deprecated Html->formTag() with Form->create()

    Revision: [4232]
    Adding additional Dispatcher fixes for named arguments; fixing parameters passed in Paginator URLs

    Revision: [4231]
    "Correcting doc comments in basics.php
    Changed the params order for _ _c()."

    Revision: [4230]
    Fixing "undefined variable" error in dispatch

    Revision: [4229]
    Adding fix for Ticket #1823

    Revision: [4228]
    adding router fix for array params, ticket #1824, removing ternary operator from HtmlHelper::link();

    Revision: [4227]
    Fixing an error in I18n class that would sometimes show up when using Scaffolded methods

    Revision: [4226]
    "A little more refactoring on HtmlHelper::checkbox()"

    Revision: [4225]
    "Fixed typo in [4221]"

    Revision: [4224]
    Adding fix for Notice: Use of undefined constant action - assumed 'action' on Scaffolded view()

    Revision: [4223]
    Adding patch from Ticket #1354

    Revision: [4222]
    "Adding a second param to bindModel and unbindModel to allow
    turning off the auto resetting of the associations.
    By default associations are always reset to the orignal models values"

    Revision: [4221]
    Adding patch from Ticket #1821

    Revision: [4220]
    Adding fix from Ticket #1808.
    Correcting language name in I10n class.
    Removed unused var from CookieComponent

    Revision: [4219]
    Adding Slovenian translation

    Revision: [4218]
    Updating CookieComponent class to allow multidimensional arrays to be stored in one cookie.
    Changed write() to allow passing $expire and $encrypt.

    Revision: [4217]
    Replacing '_view_' with 'view'

    Revision: [4216]
    Fixing parse error in PHP4

    Revision: [4215]
    Updating French, Italian, and Danish translations

    Revision: [4214]
    fixing typo in doc comment for CookieComponent::$_ _values;

    Revision: [4213]
    Adding secure CookieComponent class.

    Revision: [4212]
    fixing router args in urls, ticket #1811

    Revision: [4211]
    Fixing magic method names

    Revision: [4210]
    Merging changes from 1.1.x.x CakeSession

    Revision: [4209]
    Updating Danish translation

    Revision: [4208]
    Updating Danish translation

    Revision: [4207]
    Adding fix for I18n when $domain is specified using any of the _ _d*() functions

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@4406 3807eeeb-6ff5-0310-8944-8be069107fe0