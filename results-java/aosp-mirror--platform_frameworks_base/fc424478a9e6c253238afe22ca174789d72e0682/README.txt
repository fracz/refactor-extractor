commit fc424478a9e6c253238afe22ca174789d72e0682
Author: Gustav Sennton <gsennton@google.com>
Date:   Wed Jan 6 17:11:09 2016 +0000

    Add missing traces for WebView loading steps.

    Ever since the refactoring of WebViewFactory - to support using one out
    of a list of WebViewProviders - we cover less of the loading code with
    traces, this CL fixes this.

    Bug: 26409579

    Change-Id: I9d74321806037ea34a5ace8fc75b07ca771ab7d9