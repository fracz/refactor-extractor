commit 1ca634a544a47193dd9e57f9b3eb3e42ab0dbbf1
Author: Felipe Leme <felipeal@google.com>
Date:   Mon Nov 28 17:21:21 2016 -0800

    AutoFill Framework refactoring.

    The AutoFill Framework uses the same AssitStructure provided by the Assist API
    and so far it was using the same methods as well, both internally and externally
    (public API).

    Sharing that internal code internally is fine, but the public APIs must distinguish between the 2 cases so they can fill the assist structures accordingly (although the initial implementation still shares the same logic).

    This CL also splits the original 'auto-fill' request in 2 types of requests,
    which are set by View flags:

    - ASSIST_FLAG_SANITIZED_TEXT
    - ASSIST_FLAG_NON_SANITIZED_TEXT

    It also added new methods and callbacks to handle save requests.

    Bug: 31001899
    Test: manual verification

    Change-Id:  I4eb09099dc19a43cb7e053e64d939aed3704b410