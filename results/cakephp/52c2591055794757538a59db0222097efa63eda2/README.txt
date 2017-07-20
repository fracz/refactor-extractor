commit 52c2591055794757538a59db0222097efa63eda2
Author: phpnut <phpnut@cakephp.org>
Date:   Wed Jun 20 09:01:21 2007 +0000

    Merging fixes and enhancements into the trunk:

    Revision: [5316]
     * Adding correct fix for Ticket #2775

    Revision: [5315]
     * Correcting code structure to standards

    Revision: [5314]
     * Correcting class method names

    Revision: [5313]
     * Correcting code structure to standards

    Revision: [5312]
     * Adding fix for Ticket #2786, fixes form dateTime helper does not set year to current

    Revision: [5311]
     * Adding fix for Ticket #2792, fixes AuthComponent: Blank password at user login generates SQL error

    Revision: [5310]
     * Adding fix for Ticket #2795, fixes hidden field for checkbox.
     * Updated FormHelperTest::testCheckboxField() to reflect the correct return value

    Revision: [5309]
     * Adding fix for Ticket #2799, default error message should be i18n

    Revision: [5308]
     * Adding fix for Ticket #2737, changed TRUNCATE to TRUNCATE TABLE

    Revision: [5307]
     * Adding patch from Ticket #2642, fixes TranslateBehavior doesn't work with Model::saveField()

    Revision: [5306]
     * Adding fix for Ticket #2773, fixes Security Component requireAuth

    Revision: [5305]
     * Adding fix for Ticket #2798, fixes Hidden field causes undefined Index warning in Security Component

    Revision: [5304]
     * Adding ability to read cookies in the Controller::beforeFilter()

    Revision: [5303]
     * Adding fix for Ticket #2635, fixes Association data not correct if data is changed in afterFind()

    Revision: [5302]
     * Adding fix for Ticket #2736, fixes Core testing broken when libraries are relocated

    Revision: [5301]
     * Adding basic description to Acl shell class

    Revision: [5300]
     * Adding documentation for Acl shell and internationalizing messages

    Revision: [5299]
     * Adding documentation for ErrorHandler in Cake console and internationalizing error messages

    Revision: [5298]
     * Adding documentation for Cake console dispatcher

    Revision: [5297]
     * Adding documentation for Dispatcher

    Revision: [5296]
     * Adding documentation for CakePHP basic functions

    Revision: [5295]
     * Fixing translations function typo

    Revision: [5294]
     * Adding loadPluginModels back to dispatcher until I fix issue with loading associated plugin models.
     * Corrected form.ctp template parse error

    Revision: [5293]
     * Replacing "&&" by "AND" (ticket #2784)

    Revision: [5292]
     * Fixing forced empty element selection in FormHelper::select() (Ticket #2749)

    Revision: [5291]
     * Fixing double merging of URL parameters in PaginatorHelper::sort() (Ticket #2692)

    Revision: [5290]
     * Fixing typo (ticket #2779)

    Revision: [5289]
     * Adding fix for Ticket #2775, fixes $this->here shows incorrect value for admin route

    Revision: [5288]
     * Adding fix for Ticket #2714, fixes requestAction not working properly

    Revision: [5287]
     * Rolling back DboSource changes from 1.2 to 1.1

    Revision: [5286]
     * Adding fix for Ticket #2738, fixes Nesting level too deep error when writing object in CakeSession in php 5.2+

    Revision: [5285]
     * Refactoring DboSource::__quoteFields() and improving support for mixed string and array conditions

    Revision: [5284]
     * Adding fix for #2651, fixes query creation problem in DboAdodb

    Revision: [5283]
     * Adding patch from #2652 fixes SQLite LIMIT statement broken

    Revision: [5281]
     * fix bake index template

    Revision: [5280]
     * updating bake templates for i18n, fixes for scaffold templates

    Revision: [5279]
     * fix for scaffold without CAKE_ADMIN

    Revision: [5278]
     * Applying patch from ticket #2763, adding test for it

    Revision: [5277]
     * Refactored Scaffold: deprecated generateFieldNames, aligned scaffold and bake view templates, set var $scaffold = CAKE_ADMIN; will scaffold only the admin actions

    Revision: [5276]
     * Adding test to disprove ticket #2763

    Revision: [5275]
     * Removing localization code from core-not-found error

    Revision: [5274]
     * Fixing substring quoting in DboSource::__quoteFields()

    Revision: [5273]
     * updating Acl, should fix #2733

    Revision: [5272]
     * changing Model::create to use Set::filter

    Revision: [5271]
     * updating acl shell

    Revision: [5270]
     * updating headers

    Revision: [5269]
     * Fixing path in help text

    Revision: [5268]
     * updating bake cake admin define

    Revision: [5267]
     * Adding query conditions parenthesis fix for Ticket #2663

    Revision: [5266]
     * updating bake view templates

    Revision: [5265]
     * update to formhelper habtm fields

    Revision: [5264]
     * updating help for bake

    Revision: [5263]
     * refactoring task loading

    Revision: [5262]
     * Adding fix for #2723, this fixes Saving an array in an encrypted cookie, with the CookieComponent, prevents you from deleting it later.
     * Adding fix for #2667 move the CookieComponent::startup() implementation to CookieComponent::initialize().
     * Deprecated the use of the Controller properties to set the CookieComponent properties

    Revision: [5261]
     * Updating doc strings with clarified instructions.

    Revision: [5259]
     * udpated view to work with form helper

    Revision: [5258]
     * fixing form helper for tickets #2726 and #2659, updated tests.

    Revision: [5257]
     * Adding fix for #2640, fixes nested array issues with Router::stripEscape()

    Revision: [5256]
     * Initializing tasks before execution (ticket #2725)

    Revision: [5255]
     * Adding documentation for Component

    Revision: [5254]
     * Adding fix for #2691, fixes error caching xml files

    Revision: [5253]
     * Adding fix for #2650, fixes alias in DboAdodb::fields()

    Revision: [5252]
     * Adding fix for #2689, fixes issue when using FormHelper::submitImage with Security Component enabled

    Revision: [5251]
     * Adding fix for #2648, fixes issue with token not being regenerated

    Revision: [5250]
     * Adding fix for undefined variable in Controller::cleanUpFields()

    Revision: [5249]
     * updating view task and view template

    Revision: [5248]
     * Enabling custom pagination methods in model

    Revision: [5247]
     * Fixing problem when baking only admin functions

    Revision: [5246]
     * Fixing typo

    Revision: [5245]
     * Using $connection param when cleaning arrays (ticket #2712)

    Revision: [5244]
     * updating view task and view template

    Revision: [5243]
     * #2672: fixed model task unit test baking

    Revision: [5242]
     * fixing missing passedArgs member in controller that was not committed in [5228], fixing cleanUpFields to not set a default time or date.

    Revision: [5241]
     * fixing extra ; in bake form template

    Revision: [5240]
     * fixing arg shifting in shells for php4

    Revision: [5239]
     * updating shells with startup callback, so a shell initializes, then loads tasks, then starts up, Tasks are the same. few other minor fixes to bake, api and acl.

    Revision: [5238]
     * Adding fix for FormHelper::checkbox() not creating the hidden fields.
     * Corrected errors when multiple hidden fields used in a form.
     * Fixed SecurityComponent::_ _validatePost() that would invalidate a form when checkboxes used.

    Revision: [5237]
     * updating view task and adding view task templates

    Revision: [5236]
     * updating dbconfig, project, model, and controller tasks

    Revision: [5235]
     * updating main shell class and dispatcher

    Revision: [5234]
     * Adding fixtures for afterFind() test

    Revision: [5233]
     * Adding test case for Model::findAll() results modified in afterFind (disproves Ticket #2635)

    Revision: [5232]
     * Fixing bug introduced in [5228].

    Revision: [5231]
     * Adding changes to Controller::set() to force $camelBacked variables only if second params is null

    Revision: [5230]
     * Adding change to Controller::set() to force variable to $camelBacked

    Revision: [5229]
     * Fixing issue with bad key set in Controller::generateFieldNames() causing HABTM not to save

    Revision: [5228]
     * fixing missing passedArgs member in controller

    Revision: [5227]
     * adding pass to the default set of router params

    Revision: [5226]
     * Fixing typo (ticket #2674)

    Revision: [5225]
     * Added "Model columns" command to get a list of columns and column type for a model

    Revision: [5224]
     * Correcting date string in tests

    Revision: [5223]
     * fixing File again adding info property

    Revision: [5222]
     * Adding fix for #2657, fixes form with Textarea does not save when Security Component is enabled

    Revision: [5221]
     * fixing File tests and class

    Revision: [5220]
     * Added in more verbose output of Model->save() functionality

    Revision: [5219]
     * Added in Model->save() method along with help text explaining how to use it

    Revision: [5218]
     * Correcting loading of plugin view classes

    Revision: [5217]
     * updating acl and model cli

    Revision: [5216]
     * Removed "protected" declaration from a function...PHP 5 only

    Revision: [5215]
     * Added in dynamic unbinding, verification of existing models, verification of existing associations, added in help text and reorganized code in swich-case block now that I figured out you could put preg_matches in a case statement :)

    Revision: [5214]
     * fixing typo in cleanUpfields

    Revision: [5213]
     * Changing Helper::_ _value(); to Helper::value();
     * Removed HtmlHelper::value();
     * Changed all references to _ _value() to value();
     * Started changes needed for loading custom view classes located in a plugin

    Revision: [5212]
     * updating project task paths

    Revision: [5211]
     * updating File class and tests

    Revision: [5209]
     * Finishing vendors function to allow loading of plugin vendors files

    Revision: [5208]
     * Allowing spaces in session keys (Ticket #2639)

    Revision: [5207]
     * Implementing lazy loading of plugin models

    Revision: [5206]
     * fixing typo

    Revision: [5205]
     * adding check to previous cleanUpFields fix

    Revision: [5204]
     * updating cleanUpFields in 1.2 and 1.2 for #2632

    Revision: [5203]
     * updating file class deprecated getters, fixes for tickets #1838 and #2641, adding tests, changing Folder default directory to TMP

    Revision: [5202]
     * Moving loading of plugins controller to use loadController()

    Revision: [5201]
     * Adding documentation for Socket, Validation and Xml

    Revision: [5200]
     * Adding comments to Set

    Revision: [5199]
     * Fixing docs for Session

    Revision: [5198]
     * Formatting

    Revision: [5197]
     * Starting code changes to allow dot notation for loading of all classes

    Revision: [5196]
     * Refactoring tasks

    Revision: [5195]
     * Fixing problem with handling "0" in NumberHelper::toReadableSize() (ticket #2654)

    Revision: [5194]
     * Adding comments for Sanitize and Security

    Revision: [5193]
     * Adding comments for Router

    Revision: [5192]
     * Adding missing accessors

    Revision: [5191]
     * Adding documentation for Overloadable in both PHP versions

    Revision: [5190]
     * removing whitespace

    Revision: [5189]
     * fixing bug in paths of shell well accessing php cli through cake.php

    Revision: [5188]
     * minor enhancements like sorting in api shell

    Revision: [5187]
     * Added message when no result set is found

    Revision: [5186]
     * Made changes to properly parse output of find() and findAll()...but I don't like how it's doing it, have to find a more elegant solution later

    Revision: [5184]
     * moving beforeRender to before loadView

    Revision: [5183]
     * Added code to handle outputting associations like hasMany properly

    Revision: [5182]
     * Adding fix for #2637, fixes issue with Router::stripEscape() corrupting boolean values

    Revision: [5181]
     * updating formHelper for #1035 adding more tests, fixing some tags the would produce spaces

    Revision: [5180]
     * fixing FormHelper::month to use the comment from ticket #700

    Revision: [5179]
     * fixing bad tests in socket and some working in error messages

    Revision: [5178]
     * adding strfrotime to FormHelper::month #700, fix to radios which still need to be moved

    Revision: [5177]
     * adding and to TextHelper::toList #1934

    Revision: [5176]
     * updating html helper, deprecated methods moved to bottom

    Revision: [5175]
     * adding formHelper::checkbox
     * adding tests

    Revision: [5174]
     * typo in number test

    Revision: [5173]
     * adding number helper format and test for format and currency

    Revision: [5172]
     * Using $this->hr() instead of $this->out('------------') as per gwoo's request

    Revision: [5171]
     * updating interactive console for the grumpy guy

    Revision: [5170]
     * Added missing parameter to dynamic bind call

    Revision: [5169]
     * updating interactive console for the grumpy guy

    Revision: [5168]
     * Correcting quoting

    Revision: [5167]
     * Correcting DboSybase class methods

    Revision: [5166]
     * Adding enhancement suggestion from #1565 to all Dbo classes

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@5318 3807eeeb-6ff5-0310-8944-8be069107fe0