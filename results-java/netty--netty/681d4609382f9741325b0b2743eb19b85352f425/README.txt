commit 681d4609382f9741325b0b2743eb19b85352f425
Author: Trustin Lee <t@motd.kr>
Date:   Thu Jun 5 18:31:04 2014 +0900

    Introduce TextHeaders and AsciiString

    Motivation:

    We have quite a bit of code duplication between HTTP/1, HTTP/2, SPDY,
    and STOMP codec, because they all have a notion of 'headers', which is a
    multimap of string names and values.

    Modifications:

    - Add TextHeaders and its default implementation
    - Add AsciiString to replace HttpHeaderEntity
      - Borrowed some portion from Apache Harmony's java.lang.String.
    - Reimplement HttpHeaders, SpdyHeaders, and StompHeaders using
      TextHeaders
    - Add AsciiHeadersEncoder to reuse the encoding a TextHeaders
      - Used a dedicated encoder for HTTP headers for better performance
        though
    - Remove shortcut methods in SpdyHeaders
    - Replace SpdyHeaders.getStatus() with HttpResponseStatus.parseLine()

    Result:

    - Removed quite a bit of code duplication in the header implementations.
    - Slightly better performance thanks to improved header validation and
      hash code calculation