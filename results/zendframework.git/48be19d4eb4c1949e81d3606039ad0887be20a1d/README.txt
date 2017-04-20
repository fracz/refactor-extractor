commit 48be19d4eb4c1949e81d3606039ad0887be20a1d
Author: marc-mabe <marc-bennewitz@arcor.de>
Date:   Tue Sep 14 19:58:31 2010 +0200

    exception refactoring of Zend\Serializer:

      - moved class Zend\Serializer\Exception -> Zend\Serializer\Exception\SerializerException
      - added interface Zend\Serializer\Exception
      - Zend\Serializer\Exception\SerializerException implements Zend\Serializer\Exception
      - throw Zend\Serializer\Exception\SerializerException instead of Zend\Serializer\Exception