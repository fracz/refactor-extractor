commit 4e584df4cee8334bc371c04a67bcd0a32e2f9480
Author: Steve Block <steveblock@google.com>
Date:   Tue Apr 24 23:12:47 2012 +0100

    Fix JavaDoc style for several WebView classes

    This fixes the JavaDoc style for the following classes ...
    - CacheManager.java
    - CookieManager.java
    - GeolocationPermissions.java
    - WebResourceResponse.java
    - WebSettings.java
    - WebStorage.java
    - WebView.java

    In particular, this applies the guidelines at
    https://wiki.corp.google.com/twiki/bin/view/Main/APIDocumentation

    This should help to ensure that future JavaDoc comments use correct style,
    rather than using incorrect style for consistency.

    Note that this change does not attempt to improve the content of the JavaDoc
    comments. This will be done in later changes.

    Bug: 5461416
    Change-Id: I79e9b15a8cf3597195d58e154a7eb1bcc462944c