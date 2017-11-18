commit 3900d1c6656f617fd029b16ff1767aceaca083bb
Author: Trustin Lee <t@motd.kr>
Date:   Sat Jun 21 13:54:18 2014 +0900

    Overall refactoring of the haproxy codec

    - Convert constant classes to enum
    - Rename HAProxyProtocolMessage to HAProxyMessage for simpilicity
    - Rename HAProxyProtocolDecoder to HAProxyMessageDecoder
    - Rename HAProxyProtocolCommand to HAProxyCommand
    - Merge ProxiedProtocolAndFamity, ProxiedAddressFamily, and
      ProxiedTransportProtocol into HAProxiProxiedProtocol and its inner
      enums
    - Overall clean-up