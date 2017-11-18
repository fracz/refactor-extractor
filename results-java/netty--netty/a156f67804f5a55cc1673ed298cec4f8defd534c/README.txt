commit a156f67804f5a55cc1673ed298cec4f8defd534c
Author: Trustin Lee <trustin@gmail.com>
Date:   Sun Sep 23 17:01:31 2012 +0900

    [#624] Add varargs constructor to MessageToByteEncoder, MessageToMessage(Encoder|Decoder|Codec) to implement default isEncodable/isDecodable()

    .. and modify all their subtypes to take advantage of this improvement.