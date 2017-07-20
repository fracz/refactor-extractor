commit b32deed4aa7bac5aa3d1d84405e4a1d4dd9f2e16
Author: Jonas <github@ht-studios.de>
Date:   Mon Dec 30 16:28:10 2013 +0100

    changed FormHelper::secure() and FormHelper::end() to support attributes in the hidden CSRF-protection tags that are being generated for SecurityComponent to allow specification of additional html attributes like HTML5s "form" attribute. This allows separation of Form instantiation/controls and form data - for instance within html tables

    improved tests for testing against additional attributes for Form::secure()

    improved tests for testing against additional attributes for Form::end()

    removed array cast, fixed test

    fixed docblock format

    format

    Fixed a bug, this won't work as some forms are just empty