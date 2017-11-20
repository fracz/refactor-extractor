commit 9b08291154a55a42327e1a8efdc01e160f9c1573
Author: Misagh Moayyed <mmoayyed@unicon.net>
Date:   Thu May 5 12:34:25 2016 -0700

    Clean up Thymeleaf views and layouts (#1732)

    * Fixed check style errors

    * Fixing test cases

    * working on refresh scope

    * working on refresh scope

    * working on refresh scope

    * fixed radius dependencies

    * Added missing dependency

    * Added missing dependency

    * Added missing dependency

    * Added missing dependency

    * Added missing dependency

    * Added missing dependency

    * Added missing dependency

    * working on test cases.

    * working on test cases.

    * working on test cases.

    * working on test cases.

    * working on test cases.

    * working on test cases.

    * working on test cases.

    * Merge branch 'master' into letsboot

    # Conflicts:
    #       cas-server-core-services/src/main/java/org/jasig/cas/services/AbstractRegisteredService.java

    * google authN for mfa

    * updated docs

    * Fixed issue with geo tracking flag

    * Added a noOp attribute encoder that can be aliased to replace the default

    * Working on session management via redis and hazelcast

    * Added redis and hazelcast config for http session management

    * Added redis and hazelcast config for http session management

    * Ensure loggers are transient

    * Updated read

    * Fixed saml config

    * Fixed saml config

    * Fixed saml config

    * Fixed saml config

    * Fixing cs errors

    * Fixed cs errors

    * Fixed cs errors

    * Fix cs rules for test cases

    * Fixed saml config

    * Fixed saml config

    * Fixed saml config

    * Fixed cas3 success view by pre-formatted cas attributes

    * Clean up default service defn

    * adding additional gradle/gradlew filters

    * Fixed this access to fields

    * Fixed cs errors

    * Fixed saml config

    * dependency updates

    * cleaned up dependency config in the build

    * Fixed CS errors

    * Fixed CS errors

    * renamed header.html fragment to head.html

    * changed to point to head fragment instead of header

    * adding SASS files

    * fixing field size issue

    * adding style tweaks for :focus

    * code cleanup - removing commented out code

    * accessibility contrast fix for two buttons

    * accessibility contrast fix for header text color

    * moved the bottom fragment up so it is in the body.  Should never place tags outside of the body tag.

    * fix for styling issues

    * UI layout change

    * recompiled CSS files

    * Changed the fragment and also included font awesome

    * adding dashboard text

    * fixed issues with CAS views

    * Styled Login page

    * added thymeleaf dialect, config still needs to be turned on

    * added thymeleaf dialect, config still needs to be turned on

    * added dialect config

    * fixed issues with CAS views

    * fixed issues with CAS views

    * Updated docs

    * updated docs

    * Code clean up

    * add resources artifact

    * Revert "add resources artifact"

    This reverts commit 188c47826365bcc7ae468983246041fd31c86030.

    * Revert "Merge remote-tracking branch 'origin/letsboot' into letsboot"

    This reverts commit 0f616fa4a7417c42b154fa5d4edd75c0327f940e, reversing
    changes made to 188c47826365bcc7ae468983246041fd31c86030.

    * Revert "Revert "Merge remote-tracking branch 'origin/letsboot' into letsboot""

    This reverts commit 69990386144366904578291481d614167ab51765.

    * Revert "Revert "add resources artifact""

    This reverts commit 583f3fda530b8cb592b612c8eb7a3ae4d364e641.

    * working swf enc key sizes

    * changes to use the layout dialect

    * updated config

    * Merge branch 'master' into letsboot

    # Conflicts:
    #       cas-server-support-oauth/src/main/resources/META-INF/spring/cas-servlet-oauth.xml
    #       cas-server-webapp/src/main/resources/application.properties

    * added linter

    * fixed layout issue for this field

    * fixed layout issue for this field

    * fixed layout issue for this field

    * Fixed the intial layout of the two columns

    * fixed layout issue for this field

    * fixed layout issue for this field

    * fixed layout issue for this field

    * Fixed missing tooltip text

    * Adding gulpfile and removed package.json form the .gitignore file

    * removed package.json form the .gitignore file

    * moved around resources jar coonfig

    * fixed time-based access strategy data

    * Added modal for the reboot/refresh/shutdown buttons

    * Added common libraries

    * Conditionally checking if jQuery is included or not.

    * Removed the common libraries from head.js and added page specific D3 to the head tag

    * Adjusted the font sizes due to including Bootstrap.

    * Adding messages for the modal text on the dashboard view

    * updated gauge, and PD version

    * fixed javascript issue when clicking on server function button

    * moving towards support modules

    * moving towards support modules

    * moving towards support modules

    * moving towards support modules

    * moving towards support modules

    * Enable async processing

    * change to close the modal when click the OK button

    * Cleaned up the Login view so the messages display properly.

    * removed commented out code

    * updated docs on modules

    * added support for throttling

    * updated docs

    * updated docs

    * tweaked the alert mesages on the login screen.  Also applied a layout.html to the login screen, applied bootstrap.

    * working on docs

    * working on docs

    * fixed allignment issue for the anonymous and principle attribute fields

    * working on docs

    * working on docs

    * working on docs

    * fixed pac4j login uls

    * fixed sidebar icons

    * Adding new strings for the login page

    * cleaned up the classes

    * moved the Loin text to the messages.properties file, also hid the circle lock icon on mobile devices

    * Made login page responsive

    * Merge branch 'letsboot' of github.com:Unicon/cas into letsboot

    * fixed gradle dependencies for opensamll

    * fixed test cases

    * fixed test cases

    * fixed test cases

    * fixing test cases

    * updated logs

    * fixed test cases

    * fixing test cases

    * fixing test cases

    * fixing test cases

    * fixing test cases

    * fixing test cases

    * fixing test cases

    * fixing test cases

    * fixing test cases

    * Merge branch 'master' into letsboot

    # Conflicts:
    #       cas-server-core-tickets/src/main/java/org/jasig/cas/ticket/registry/AbstractTicketDelegator.java
    #       cas-server-core-tickets/src/main/java/org/jasig/cas/ticket/registry/AbstractTicketRegistry.java
    #       cas-server-core-tickets/src/main/java/org/jasig/cas/ticket/registry/ProxyGrantingTicketDelegator.java
    #       cas-server-core-tickets/src/main/java/org/jasig/cas/ticket/registry/ProxyTicketDelegator.java
    #       cas-server-documentation/installation/Monitoring-Statistics.md
    #       cas-server-extension-clearpass/src/main/java/org/jasig/cas/extension/clearpass/TicketRegistryDecorator.java
    #       cas-server-support-ehcache/src/main/java/org/jasig/cas/ticket/registry/EhCacheTicketRegistry.java
    #       cas-server-support-jpa-ticket-registry/src/main/java/org/jasig/cas/config/JpaTicketRegistryConfiguration.java
    #       cas-server-support-jpa-ticket-registry/src/main/java/org/jasig/cas/ticket/registry/support/JpaLockingStrategy.java
    #       cas-server-support-jpa-ticket-registry/src/test/resources/jpaSpringContext.xml
    #       cas-server-support-memcached/src/main/java/org/jasig/cas/ticket/registry/MemCacheTicketRegistry.java
    #       cas-server-support-oauth/src/main/java/org/jasig/cas/OAuthApplicationContextWrapper.java
    #       cas-server-support-oauth/src/test/java/org/jasig/cas/support/oauth/web/OAuth20AccessTokenControllerTests.java
    #       cas-server-support-spnego/src/main/java/org/jasig/cas/support/spnego/authentication/handler/support/JcifsConfig.java
    #       cas-server-webapp/src/main/resources/application.properties

    * fixed test cases

    * fixed test cases

    * fixed test cases

    * fixed test cases

    * fixed test cases

    * fixed test cases

    * fixed test cases

    * updated readme

    * fixed checkstyle

    * upgrade to gradle 2.13

    * upgrade to gradle 2.13

    * added remote access strategy

    * added remote access strategy

    * updated docs

    * working on test cases

    * provide slovak localization (#1722)

    # Conflicts:
    #       cas-server-webapp/src/main/webapp/WEB-INF/view/jsp/default/ui/casLoginView.jsp

    * Merged: Add LDAP support for Acceptable Usage Policy (AUP) functionality

    * Merged: Add LDAP support for Acceptable Usage Policy (AUP) functionality

    * Cleaning up codebase based on JDK8 constructs

    * moved and renamed the save form pieces to a subfolder to make them easier to find

    * Updated framgent pat to reference the new path and names

    * fixed layout issue with fields

    * adding missing glyphicon fonts

    * added the select type, unauthorized redirect url and case insensitive fields.  Wired up to the model.

    * Adding the multiAuth Pane, Properties Pane and Rejected Attributes to the view

    * Added MultiAuth Failure Mode dropdown values

    * Adding SAML options panel

    * updated image

    * updated checkstyle

    * Merge branch 'master' into letsboot

    # Conflicts:
    #       cas-server-support-oauth/src/main/java/org/jasig/cas/support/oauth/web/BaseOAuthWrapperController.java

    * changed to use the template/layout file instead of monitoring/layout

    * changed files to use layout file

    * Adding SASS compilation to the cradle build process.  Removed the css folder and files since they should now be compiled at build time.

    * updated build script

    * Adding Node config to force the node plugin to download Node

    * added display names for pac4j providers

    * added support for Google Analytics

    * updated docs

    * updated docs

    * updated docs

    * adding login providers fragment

    * Adding bootstrap to the SCSS process.  Also adding bootstrap-social

    * included the _bootstrap.scss file.  Added custom bootstrap-social buttons

    * Adjusted the padding for the list provider buttons

    * Updated the logic to make it cleaner and to handle unknown types.

    * Including the list-providers fragment

    * removed the list-providers since they are now included elsewhere in the layout

    * Removed reference to bootstrap CDN since we are now building it in the cas.scss

    * refactored to pull in bootstrap via npm.
    removed the devDepencies entries

    * refactored to use the bootstrap-sass npm module

    * removed bootstrap from vendor directory since it is being included in the npm_install now

    * removed bootstrap vendor import

    * fix for the YubiKey form

    * readied the bootstrap CDN for now

    * updated version # to 1.0.3

    * changed font size for input fields

    * changed the order of CSS imports

    * changed the user back to "casuser"

    * Styling the general error view to use the bootstrap alert alert-danger styling

    * added init module

    * SCSS Changes:
    removed bullets from list providers.
    moved the A styles outside of the UL

    HTML changes:
    added title attributes to the link
    added logic to display buttons if pac4jUrls list is <= 5 otherwise display icons only to save space.

    * fixed test cases

    * fixed test cases

    * fixed test cases

    * Fixed merge issues

    * ignoring nodejs file

    * cleaned up node build