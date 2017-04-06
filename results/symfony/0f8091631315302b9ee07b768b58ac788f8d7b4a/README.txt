commit 0f8091631315302b9ee07b768b58ac788f8d7b4a
Merge: 164c1cb 7f02304
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Sep 30 17:35:08 2013 +0200

    feature#6554 [Security] Added Security\Csrf sub-component with better token generation (bschussek)

    This PR was merged into the master branch.

    Discussion
    ----------

    [Security] Added Security\Csrf sub-component with better token generation

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | TODO

    **Update September 27, 2013**

    This PR simplifies the CSRF mechanism to generate completely random tokens. A random token is generated once per ~~intention~~ token ID and then stored in the session. Tokens are valid until the session expires.

    Since the CSRF token generator depends on `StringUtils` and `SecureRandom` from Security\Core, and since Security\Http currently depends on the Form component for token generation, I decided to add a new Security\Csrf sub-component that contains the improved CSRF token generator. Consequences:

    * Security\Http now depends on Security\Csrf instead of Form
    * Form now optionally depends on Security\Csrf
    * The configuration for the "security.secure_random" service and the "security.csrf.*" services was moved to FrameworkBundle to guarantee BC

    In the new Security\Csrf sub-component, I tried to improve the naming where I could do so without breaking BC:

    * CSRF "providers" are now called "token generators"
    * CSRF "intentions" are now called "token IDs", because that's really what they are

    ##### TODO

    - [ ] The documentation needs to be checked for references to the configuration of the application secret. Remarks that the secret is used for CSRF protection need to be removed.
    - [ ] Add aliases "csrf_token_generator" and "csrf_token_id" for "csrf_provider" and "intention" in the SecurityBundle configuration
    - [x] Make sure `SecureRandom` never blocks for `CsrfTokenGenerator`

    Commits
    -------

    7f02304 [Security] Added missing PHPDoc tag
    2e04e32 Updated Composer dependencies to require the Security\Csrf component where necessary
    bf85e83 [FrameworkBundle][SecurityBundle] Added service configuration for the new Security CSRF sub-component
    2048cf6 [Form] Deprecated the CSRF implementation and added an optional dependency to the Security CSRF sub-component instead
    85d4959 [Security] Changed Security HTTP sub-component to depend on CSRF sub-component instead of Form
    1bf1640 [Security] Added CSRF sub-component