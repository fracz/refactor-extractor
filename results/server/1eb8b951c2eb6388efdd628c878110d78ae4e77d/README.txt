commit 1eb8b951c2eb6388efdd628c878110d78ae4e77d
Author: Arthur Schiwon <blizzz@arthur-schiwon.de>
Date:   Wed Aug 10 15:21:25 2016 +0200

    more admin page splitup improvements

    * bump version to ensure tables are created
    * make updatenotification app use settings api
    * change IAdmin::render() to getForm() and change return type from Template to TemplateResponse
    * adjust User_LDAP accordingly, as well as built-in forms
    * add IDateTimeFormatter to AppFramework/DependencyInjection/DIContainer.php. This is important so that \OC::$server->query() is able to resolve the
    constructor parameters. We should ensure that all OCP/* stuff that is available from \OC::$server is available here. Kudos to @LukasReschke
    * make sure apps that have settings info in their info.xml are loaded before triggering adding the settings setup method