commit e96e75ec0c7a5d06902756e53317c3450fb7e532
Author: Misagh Moayyed <mmoayyed@unicon.net>
Date:   Thu Sep 7 11:56:40 2017 -0700

    Management web app bug fixes (#2916)

    * Upgrade to Angular front end code

    * Remove json-service-registry from build.gradle

    * Fixes for checkstlye

    * Fixed test for new updateOrder method

    * Added npm install and ng build to gradle build

    * Fixed Codacy complaints in tab.service.ts

    * Fixes for codacy bot and checkStyle

    * Fixes for checkStyleTest

    * Fixes for errors found by reviewers

    * Fixes for SamlServicesPanel

    * Fixed angular tests

    * Removed unused fields in form.ts
    Added angularTest task

    * Refactored Domain classes

    * Fixed Controller Unit Tests
    Fixed angular production build

    * Converted RegexRegisteredService

    * Converted Oauth, Oidc, SAML and WsFed

    * Used injectable Data object instead of @Input attributes

    * Refactor string literals to Enums

    * Refactored and cleaned FormComponent

    * Changed form to use Material UI components

    * Cleaned up LogoutType errors
    Deleted Mappers

    * Added Metadata Expiration field to SamlClient

    * Moved Service Type to Basics screen
    Refacored OAuth,OIDC,SAML and WS Fed into their own tab

    * Mapping for RegisterdServiceView

    * Refactoring of Forms
    Clean up of Validation
    Set correct defaults for some values

    * Refactored Delete Modal to use MdDialog in separate template
    Refactored Services Screen to use Material UI

    * Background color

    * Mgmt issues

    * Merge branch 'master' of https://github.com/apereo/cas into mgmt-angular-json

    # Conflicts:
    #       webapp-mgmt/cas-management-webapp-support/build.gradle

    * Merge branch 'master' of https://github.com/apereo/cas into mgmt-angular-json

    # Conflicts:
    #       webapp-mgmt/cas-management-webapp-support/build.gradle

    * Fix for issue #3 - encryptUsername

    * Fix for issue #4 - enforce public key for credential and PGT release

    * Fix for issue #5 - Tab Labels

    * Fix for issue #6 - Change Service URL label and tooltip when SAML client is chosen

    * Updated Angular and Material libraries
    fixed issue #11 - Service property keys has been coded as an Autocomplete widget

    * fixed issue #7 - Changed OIDC Encryption options to be Autcomplete

    * Closes issue #12 - Pairwaise subjectId
    Fixes the width of text inputs

    * Closes issue #13 - Support Consent Policy

    * Closes issue #16 - Release Authentication Attributes
    Provides front-end for issue #14 - Surrogate Access strategy

    * Closes issue #18 - Add support for SAML skip options

    * Closes issue #19 - Add support for SAML2 EntityAttributes

    * Small refactor of <md-input-container> to <md-form-field> for latest Material update

    * Removal of Alert component in favor of Material Snackbar

    * Closes Issue #14 - Surrogate Access Strategy

    * Closes Issue #14 - Surrogate Access Strategy

    * Changed buid.gradle in mgmt-app so only ng build --prod runs for a build
    Fixed Test case

    * Closes issue #24 - Tymeleaf templates for form removed

    * Closes issue #21 - Saml Attribute Name Formats coded incorrectly

    * Closes issue #22 - Allows addition to Required Attributes under Access Strategy

    * Merge branch 'master' of https://github.com/apereo/cas into mgmt-angular-json

    # Conflicts:
    #       api/cas-server-core-api-services/src/main/java/org/apereo/cas/services/LogoutType.java
    #       core/cas-server-core-logout/src/main/java/org/apereo/cas/logout/DefaultSingleLogoutServiceMessageHandler.java
    #       webapp-mgmt/cas-management-webapp-support/src/main/java/org/apereo/cas/mgmt/services/web/ManageRegisteredServicesMultiActionController.java
    #       webapp-mgmt/cas-management-webapp-support/src/main/java/org/apereo/cas/mgmt/services/web/RegisteredServiceSimpleFormController.java
    #       webapp-mgmt/cas-management-webapp-support/src/main/java/org/apereo/cas/mgmt/services/web/beans/FormData.java
    #       webapp-mgmt/cas-management-webapp-support/src/main/java/org/apereo/cas/mgmt/services/web/factory/DefaultRegisteredServiceFactory.java
    #       webapp-mgmt/cas-management-webapp-support/src/test/java/org/apereo/cas/services/web/ManageRegisteredServicesMultiActionControllerTests.java
    #       webapp-mgmt/cas-management-webapp-support/src/test/java/org/apereo/cas/services/web/RegisteredServiceSimpleFormControllerTests.java
    #       webapp-mgmt/cas-management-webapp/build.gradle
    #       webapp-mgmt/cas-management-webapp/src/app/form/access-strategy/access-strategy.component.html
    #       webapp-mgmt/cas-management-webapp/src/app/form/rejectedattributes/rejectedattributes.component.html
    #       webapp-mgmt/cas-management-webapp/src/app/form/rejectedattributes/rejectedattributes.component.ts
    #       webapp-mgmt/cas-management-webapp/src/app/form/samlclient/samlclient.component.html
    #       webapp-mgmt/cas-management-webapp/src/app/form/samlservicespane/samlservicespane.component.html
    #       webapp-mgmt/cas-management-webapp/src/app/form/samlservicespane/samlservicespane.component.ts
    #       webapp-mgmt/cas-management-webapp/src/locale/messages.xlf