commit 40f89bf0bcd5e0d5b7e7e532da7dfddad8b7f009
Author: mgp25 <me@mgp25.com>
Date:   Fri Aug 25 01:26:49 2017 +0200

    Instagram: setAnonymousUser() for non authenticated requests

    Some functions like the ones related to registration, are non
    authenticated requests, therefore to emulate this process in the API we
    can create a fictitious username that will make the trick.

    It will only send the pre login flow to obtain the csrftoken and the
    you can use functions like checkUsername() or checkEmail().

    This fictitious user is ONLY to make non authenticated requests as the
    ones found in Requests/Registration

    I’ll improve this when i have some spare time.