commit 9ea4583661793f2f95c84aa39022dd8a64bf8821
Author: Christopher Smith <chris@jalakai.co.uk>
Date:   Wed Apr 1 19:11:34 2015 +0100

    Fix up test_simplemail()
    - refactor Mailer class to allow unit tests to access token
      replacements
    - use CRLF in signature string per rfc2045 6.8
    - add email signature to the expected mail body
    - apply appropriate chunksplit to the expected mail body
    - ensure regex is quoted - '/' is a legal base64 character