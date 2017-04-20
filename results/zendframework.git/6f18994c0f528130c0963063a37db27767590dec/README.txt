commit 6f18994c0f528130c0963063a37db27767590dec
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Wed Jan 11 17:14:53 2012 -0600

    Performance improvements/simplification of CallbackHandler

    - Do not perform lazy instantiation for callbacks
    - DO validate that static callbacks are actually valid
    - Use call_user_func vs _array variant for small argument sets
      - Up to 3 arguments
      - In PHP 5.4, simply call the callable directly in these cases
    - Rename "options" to "metadata"
    - Do not accept $event as first argument; do allow passing it as
      metadata