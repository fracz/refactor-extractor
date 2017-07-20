commit fb52e0e5bdf750c151792f70d0eeb50bfaf42963
Author: Taylor Otwell <taylor@laravel.com>
Date:   Tue Dec 27 10:29:18 2016 -0600

    Validation Refactor (#17005)

    This refactors the validation component into a few traits and classes. Mainly to separate out areas of focus so the main logic is easier to read. Separated message formatting, replacement formatting, and validation rules.