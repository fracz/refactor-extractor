commit e0add0b68fdb0e3e8ebf8d6516193514864a6632
Author: beckyvb <beckyvanbussel@gmail.com>
Date:   Tue Mar 21 17:28:48 2017 -0400

    Add an optional site setting to send users to /leaving when clicking on a ugc link

    * Breaks the embed code replacement into its own function `embedReplacement($url)`, and optimizes and refactors the embed code replacement.
    * Adds a `matchesTrustedDomain($url)` function to test whether a given url matches a trusted domain in the `linksCallback` function.
    * Adds a redirect to `/leaving` to any non-trusted link if the `Garden.Format.WarnLeaving` config is set.
    * Adds a `reverse` param to the toggle to handle "disable"-type config settings.
    * Adds controls for the `Garden.Format.WarnLeaving` and `Garden.Format.DisableUrlEmbeds` config settings to the Advanced settings page in the dashboard.
    * Adds a title to the `/leaving` page.

    Bonus:
    * Fixes double-urling in the SafeRedirect function.
    * Fixes Wistia and Twitch embeds

    Closes https://github.com/vanilla/vanilla/issues/5334