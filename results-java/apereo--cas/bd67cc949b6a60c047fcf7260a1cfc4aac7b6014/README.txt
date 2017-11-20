commit bd67cc949b6a60c047fcf7260a1cfc4aac7b6014
Author: arbner <26866251+arbner@users.noreply.github.com>
Date:   Tue Oct 17 13:49:51 2017 +0200

    add review option for consent (#2934)

    * initial

    * mvc, security and config wiring

    review pac4j config, simplify controller

    move SeecurityInterceptor to common place

    move CasSecurityInterceptor to core package

    * add GET endpoint for consentDecisions

    * renamed Overview to Review

    * UI: initial table layout and js

    * UI: add link to login page

    * refactor CasSecurityInterceptor

    added postHandle to hide request parameters etc to interceptor
    adjusted status endpoint config to also use this
    moved Interceptor to cas-server-support-pac4j-core

    refactor pac4j configuration

    return decision plus attributes

    * add deletion to consent repository

    * UI: opening details

    * enable deletion in consent repo

    * UI: enable deletion

    * work on UI

    add i18n
    extend deletion
    improve row detail
    add tooltips

    * more on UI

    add logout button

    fix tooltips

    * add logout to controller, disable consent for consent

    * review UI

    * pac4j: use indirect client

    now the correct callback url is used

    * fix for dashboard posthandle

    * add deletion to groovy repo

    * change consentDecision's TimeUnit to ChronoUnit

    * change date label

    * rename auto-registered service

    * change rest consent deletion to http delete

    * docs

    * i18n for consentView

    minor changes to ReviewView to reuse common strings

    * minor edits

    * change endpoint to consentReview, logout through pac4j manager

    * fix sorting by date, parsing the date

    * handle getConsentDecisions asynchronous

    * fix for table width

    * change label to primary

    * fix dashboard integration