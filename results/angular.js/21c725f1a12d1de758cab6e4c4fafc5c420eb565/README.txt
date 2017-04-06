commit 21c725f1a12d1de758cab6e4c4fafc5c420eb565
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Wed Feb 15 17:16:02 2012 -0800

    refactor(forms): Even better forms

    - remove $formFactory completely
    - remove parallel scope hierarchy (forms, widgets)
    - use new compiler features (widgets, forms are controllers)
    - any directive can add formatter/parser (validators, convertors)

    Breaks no custom input types
    Breaks removed integer input type
    Breaks remove list input type (ng-list directive instead)
    Breaks inputs bind only blur event by default (added ng:bind-change directive)