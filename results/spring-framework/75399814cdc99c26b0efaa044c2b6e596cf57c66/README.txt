commit 75399814cdc99c26b0efaa044c2b6e596cf57c66
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Mon Mar 21 14:02:52 2016 +0100

    Improve Jaxb2Decoder

    - Introcuces XmlEventDecoder which decodes from DataBuffer to
      javax.xml.stream.events.XMLEvent. It uses the Aalto async XML API if
      available, but falls back to a blocking default if not.

    - Refacors Jaxb2Decoder to use said XmlEventDecoder, and split the
      stream of events into separate substreams by using the JAXB annotation
      value, one stream for each part of the tree that can be unmarshaled to
      the given type.

    - Various improvements in the JAXB code.