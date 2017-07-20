commit aba50d3e89f72f34bed7d66ad4660fcc6aeb0057
Author: phpnut <phpnut@cakephp.org>
Date:   Fri Mar 9 23:26:37 2007 +0000

    Merging fixes into 1.2.x.x trunk:

    Revision: [4604]
    Adding new CLI tools suite

    Revision: [4602]
    adding caching for elements

    Revision: [4601]
    updating domId to always return an array

    Revision: [4600]
    Adding patch for multipart boundary formatting (thanks sdevore)

    Revision: [4599]
    Fixing up test for HtmlHelper::selectTag() (Deprecated methods will still be tested until they are removed)

    Revision: [4598]
    fixing latest bug in router reverse mapping

    Revision: [4597]
    fixing links in bake

    Revision: [4596]
    refactoring cleanUpFields even though its going to be removed, uncommenting the ajax and abre routes in router

    Revision: [4595]
    Adding and updating core error messages

    Revision: [4594]
    Renaming dummy controller, commenting out test for deprecated function

    Revision: [4593]
    Updating AjaxHelper::submit() with FormHelper reference

    Revision: [4592]
    Fixing 'ambiguous column' bug in Model::findCount()

    Revision: [4591]
    Adding core load error in webroot/index, and updating setting for debug reference in Auth

    Revision: [4590]
    Adding fix for Ticket #2198

    Revision: [4589]
    Adding fix for Ticket #2101.
    Changed Helper::loadConfig() to accept a param with the name of the file to load

    Revision: [4588]
    Adding Helper::clean() to strip all harmful tags from output.
    CHanged Sanitize::escape() to strip off the beginning and ending ' that is added by Dbo*::value()

    Revision: [4586]
    Removing error view path setting from viewPaths in Configure class

    Revision: [4585]
    Adding fix for Ticket #2090.
    Refactored view loading.

    Revision: [4584]
    fixing bake for ticket #2123

    Revision: [4583]
    Moved all deprecated methods to end of file.
    Added notices for all methods that are deprecated.
    Added fix for Ticket #2185.

    Revision: [4582]
    Adding fix for Ticket #2161

    Revision: [4581]
    Moving deprecated methods to end of file.
    Corrected fix for Ticket #2188

    Revision: [4580]
    Adding fix for Ticket #2188

    Revision: [4579]
    Moving date/time related form methods to FormHelper.
    Fixed Ticket #2189.

    Revision: [4578]
    Removing $this->Html-> in FormHelper::textarea()

    Revision: [4577]
    Adding fix for Ticket #2156

    Revision: [4576]
    Adding fix for Ticket #2055.

    Revision: [4575]
    Adding fix for Ticket #2130, Ticket #2168, Ticket #2178
    Adding beforeRender() callback for helpers.
    Adding fix for Set::diff();

    Revision: [4574]
    Adding fix for Ticket #2084

    Revision: [4573]
    Removing debug code from Inflector

    Revision: [4572]
    Adding patch from Ticket #2176, extracts the sequence name from the default value for the primary key and returns it's last value

    Revision: [4571]
    Adding fix for Ticket #1442.
    Controller components are now initialized in cached views

    Revision: [4570]
    Adding fix for Ticket #2118.
    Removing check for inflections.php file, this file has been included in releases since v 1.0.0.2312

    Revision: [4569]
    Adding fix from Ticket #2074

    Revision: [4568]
    Moving loading of Debugger class to Configure::write() so it loads only when DEBUG > 0

    Revision: [4567]
    Refactoring Model::getColumnType() and Model::getColumnTypes()

    Revision: [4566]
    Updating HttpSocket::serialize() to use Router::queryString(), updating Auth to use salted hashes, and resolving RequestHandler conflict for Ajax-based requests to custom content types

    Revision: [4565]
    Adding bootstrap check for unique application salt value, and updating File error message

    Revision: [4564]
    Patching Configure to bootstrap without an app

    Revision: [4563]
    Fixing class "Controller" not found error (ticket #2181)

    Revision: [4562]
    Fixing default JavaScript template, and code generation

    Revision: [4561]
    Initial implementation code for dynamic POST variable names

    Revision: [4560]
    updating bake, ticket #2067

    Revision: [4559]
    Temporarily disabling Ajax/bare routes

    Revision: [4558]
    Deprecating HtmlHelper::selectTag()

    Revision: [4557]
    Adding testcase for HTML helper

    Revision: [4556]
    Fixing reverse route matching in Router::url() and updating Router tests

    Revision: [4555]
    Fixing layout search paths for alternate content types

    Revision: [4554]
    Refactoring Router

    Revision: [4553]
    Updating Router test case

    Revision: [4552]
    Finished retro-fitting CakeSession::del() (Ticket #2163)

    Revision: [4551]
    Fixing conflict between JS script caching and dynamic updating of 'magic' Ajax div's

    Revision: [4550]
    Initial import of JsHelper JavaScript generator. PHP5-only until further notice.

    Revision: [4549]
    Removing deprecated settings key 'connect' and replacing with 'persistent' in default database connection class

    Revision: [4548]
    Forcing cached script output to be written inline (JavascriptHelper)

    Revision: [4547]
    Beefing up documentation blocks.

    Revision: [4546]
    Importing initial draft of HttpSocket class, as implemented by Felix Geisendorfer

    Revision: [4545]
    Removing eval() calls from Session and replacing with Set methods

    Revision: [4544]
    Fixing AuthComponent::redirect() to redirect back to $loginRedirect instead of $loginAction

    Revision: [4543]
    Corrected doc comment in Validation::blank()

    Revision: [4542]
    Adding core Debugger object

    Revision: [4541]
    Adding fix for secure cookie not being destroyed

    Revision: [4540]
    Replacing "thtml" with "ctp" in error templates

    Revision: [4539]
    More debug styles

    Revision: [4538]
    Adding debug CSS

    Revision: [4537]
    Adding fix for #2137

    Revision: [4536]
    Adding fix for #2070

    Revision: [4535]
    Setting max_execution_time to 0 on all CLI scripts

    Revision: [4534]
    Adding fix for #1815

    Revision: [4533]
    Adding fix for #2068.
    Renaming Translate to TranslateBehavior.
    Added empty class methods to TranslateBehavior.

    Revision: [4532]
    Proofing the comments in core.php. Fixing spelling errors and trying to reword sections.

    Revision: [4531]
    Adding some clarifications to controller doc comments.

    Revision: [4530]
    Removing 'showParents' attribute from FormHelper::select() output (Ticket #2134)

    Revision: [4529]
    Fixing instance references in Set method calls

    Revision: [4528]
    Fixing typo (ticket #2144)

    Revision: [4527]
    Renaming $__transactionStarted to $_transactionStarted (ticket #2141)

    Revision: [4526]
    Renaming $__sources to $_sources (ticket #2140)

    Revision: [4525]
    Moving Translate class to the behaviors directory.
    Moving i18n sql to app/config/sql

    Revision: [4524]
    Fixing event:Selectors implementation for Ajax reloads

    Revision: [4523]
    Fixing implementation of Set::remove() for instance and static calling

    Revision: [4522]
    Adding key fix for Model::create() (Ticket #2106), and adding warning message for model load failures

    Revision: [4521]
    Refactoring ACL and tree code for query optimization changes

    Revision: [4520]
    Fixing the display of SQL errors when DEBUG > 0

    Revision: [4519]
    Adding the DB2 driver submitted by Daniel Krook

    Revision: [4518]
    Updating core Auth/ACL error messages

    Revision: [4517]
    Optimizing ACL node querying

    Revision: [4516]
    Adding parameters for AjaxHelper::sortable()

    Revision: [4515]
    Implementing Set::check() and Set::remove()

    Revision: [4514]
    Refactoring SessionHelper and SessionComponent

    Revision: [4513]
    Adding Model::bindNode() callback for optional authorization aliasing

    Revision: [4512]
    Refactoring SessionHelper and SessionComponent

    Revision: [4511]
    Refactoring CakeSession to remove eval()

    Revision: [4510]
    Fixing issue with Set::insert(), and changing CakeSession::_ _writeSessionVar() to use Set::insert()

    Revision: [4509]
    Implementing Set::insert() for dynamic array writes

    Revision: [4508]
    Implementing controller (CRUD)-based authorization

    Revision: [4507]
    Adding Set class dependency to Session

    Revision: [4506]
    Refactoring SessionComponent::write() to allow the first param to be an array.
    Removing eval() from CakeSession::returnSessionVars() and CakeSession::readSessionVar()

    Revision: [4505]
    Refactoring Auth

    Revision: [4504]
    Adding AjaxHelper::sortable() fix for Ticket #2100

    Revision: [4503]
    Updating AclComponent for distributed ACL system

    Revision: [4502]
    Refactoring ACL system to require less model code

    Revision: [4501]
    Refactoring AclBehavior

    Revision: [4500]
    Fixing object support for Set::diff()

    Revision: [4499]
    Adding null value checks to Set::diff()

    Revision: [4498]
    Moving Object::enum() to Set::enum()

    Revision: [4497]
    Fixing typo, those happen when you are in a coma

    Revision: [4496]
    fixing last commit

    Revision: [4495]
    Adding Object::enum()

    Revision: [4494]
    Fixing ACL node creation for multiple insertions

    Revision: [4493]
    Fixing permissions setting for ACL

    Revision: [4492]
    Loading additional ACL model classes in AclBehavior

    Revision: [4491]
    Adding define ACL_DATABASE

    Revision: [4490]
    Adding missing headers

    Revision: [4489]
    Updating ACL SQL table definitions

    Revision: [4488]
    Adding Permission join model for ACL nodes

    Revision: [4487]
    Importing new ACL system

    Revision: [4486]
    Changing Object::set() to non-conflicting _set()

    Revision: [4485]
    Removing need for plugins to need a model

    Revision: [4484]
    Removing password data from user record access in AuthComponent

    Revision: [4483]
    Implementing AuthComponent::$autoRedirect()

    Revision: [4482]
    Adding tests for DboSource and AjaxHelper, and fixing code formatting in XmlHelper

    Revision: [4481]
    Adding object support to Set::extract()

    Revision: [4480]
    Fixing order of operations for AuthComponent

    Revision: [4479]
    Changing param order in Model::updateAll() so it makes more sense

    Revision: [4478]
    Fixing $conditions === true in DboSource

    Revision: [4477]
    Reverting changes made in last commit

    Revision: [4476]
    Implementing Model::deleteAll()

    Revision: [4475]
    Refactoring set() method into Object, and adding $conditiions === true translation in DboSource

    Revision: [4474]
    Adding Helper::webroot() to return correct url if theme is used

    Revision: [4473]
    Adding check for webroot/themed/[theme name]

    Revision: [4472]
    Fixing theme class

    Revision: [4471]
    Changing check for theme to empty

    Revision: [4470]
    Updating theme class to allow setting theme to null and deafult locations will be used.

    Revision: [4469]
    Fixing Configure::store().
    Original version of method would set the array for a $key $value par incorrectly

    Revision: [4468]
    fixing issue with bake reading tables

    Revision: [4467]
    Allowing 'error' => false to disable validation error output in FormHelper::input() (Ticket #2071)

    Revision: [4466]
    Updating Behavior class def with new callback param

    Revision: [4465]
    Refactoring Model::save() and adding $created parameter to afterSave()

    Revision: [4464]
    Restructuring Inflector tests, and adding test for Ticket #2068

    Revision: [4463]
    Adding Inflector test for Ticket #2061

    Revision: 4461
    Fixing null child reference issue in XML

    Revision: [4460]
    Updating View class properties, and committing additional core tests

    Revision: [4459]
    Additional Ajax test

    Revision: [4458]
    Adding AjaxHelper tests

    Revision: [4457]
    Adding tests directory to scripts/templates/skel

    Revision: [4456]
    Fixing undefined index 'action' notice in Router

    Revision: [4455]
    Securing AuthComponent against re-POSTing password data

    Revision: [4453]
    Adding missing loadModel() (ticket #2058)

    Revision: [4452]
    Fixing typo (ticket #2057)

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@4605 3807eeeb-6ff5-0310-8944-8be069107fe0