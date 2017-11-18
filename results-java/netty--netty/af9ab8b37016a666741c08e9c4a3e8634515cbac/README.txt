commit af9ab8b37016a666741c08e9c4a3e8634515cbac
Author: Trustin Lee <t@motd.kr>
Date:   Sun Apr 20 17:20:00 2014 +0900

    Feed only a single SSL record to SSLEngine.unwrap()

    Motivation:

    Some SSLEngine implementations violate the contract and raises an
    exception when SslHandler feeds an input buffer that contains multiple
    SSL records to SSLEngine.unwrap(), while the expected behavior is to
    decode the first record and return.

    Modification:

    - Modify SslHandler.decode() to keep the lengths of each record and feed
      SSLEngine.unwrap() record by record to work around the forementioned
      issue.
    - Rename unwrap() to unwrapMultiple() and unwrapNonApp()
    - Rename unwrap0() to unwrapSingle()

    Result:

    SslHandler now works OpenSSLEngine from finagle-native.  Performance
    impact remains unnoticeable.  Slightly better readability. Fixes #2116.