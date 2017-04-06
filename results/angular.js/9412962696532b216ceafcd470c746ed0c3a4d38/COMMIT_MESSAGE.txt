commit 9412962696532b216ceafcd470c746ed0c3a4d38
Author: Alex Dobkin <dobkin@google.com>
Date:   Thu Sep 15 14:45:47 2016 -0700

    fix($sce): consider document base URL in 'self' URL policy

    Page authors can use the `<base>` tag in HTML to specify URL to use as a base
    when resovling relative URLs. This can cause SCE to reject relative URLs on the
    page, because they fail the same-origin test.

    To improve compatibility with the `<base>` tag, this commit changes the logic
    for matching URLs to the 'self' policy to allow URLs that match the protocol and
    domain of the base URL in addition to URLs that match the loading origin.

    **Security Note:**
    If an attacker can inject a `<base>` tag into the page, they can circumvent SCE
    protections. However, injecting a `<base>` tag typically requires the ability to
    inject arbitrary HTML into the page, which is a more serious vulnerabilty than
    bypassing SCE.

    Fixes #15144

    Closes #15145