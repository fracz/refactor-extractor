commit 1bdce0b386033d09eeea794d17a9e216f7a17ac4
Author: swoeste <dev@swoeste.de>
Date:   Thu Aug 3 09:08:58 2017 +0200

    Adds support for local URLs for single logout (#2821)

    * Changes the view resolver for registered serivces to consider the default theme

    The configured default theme ("cas.theme.defaultThemeName") was only
    considered by the ThemeResolver but not by the ViewResolver for
    (registered) services. As specially in the case a non-service (or an
    unregistered service) like /authentication is called it will use no
    theme at all. The behaviour of the
    RegisteredServiceThemeBasedViewResolver is changed so that it will
    consider the default view name for services and non-services. For a
    service it will first check the configured theme of the service, if it
    does not exist it will check the default theme and if this also does not
    exist it will use no theme. For a non-service it will first check the
    default theme and if it does not exist it will use no theme like before.

    * fixes formatting

    * fixes checkstyle

    * fixed invalid javadoc

    * fixed wrong merged file

    * Adds support for local URLs in single logout

    With the current implementation of DefaultSingleLogoutServiceLogoutUrlBuilder it is not possible to use CAS in a local(host) setup.

    Example: We use CAS and all its services behind an apache server. These services and CAS itself are only accessible via http(s)://localhost/.. on that particular environment. User access is only possible via the apache server which acts as a proxy for http(s)://foo.bar/.. to http://localhost/.. . This setup works perfectly fine except for the single logout.

    The current implementation of the DefaultSingleLogoutServiceLogoutUrlBuilder uses the org.apache.commons.validator.routines.UrlValidator to check if the logout URL is valid. Problem at this point is that the default UrlValidator does not allow local URLs.

    With this commit the creation of the UrlValidator is changed so that it could be configured whether a service allows local URLs or not. If a particular service allows local URLs the UrlValidator is created as "new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS)".

    The configuration could be set as ""localLogoutUrlAllowed" : true" in the service configuration.

    * apply suggestions (refactoring)

    * apply suggestions from review

    * apply suggestions from review

    * corrected code indentation

    * removed trailing spaces

    * renamed test file according to checkstyle issue

    * updated test configuration to fix test failures

    * updated test configuration to fix test failures

    * updated test configuration to fix test failures