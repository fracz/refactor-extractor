commit fee18a06088f4b94969791d81e5a47475d9cd535
Author: lsmith77 <smith@pooteeweet.org>
Date:   Wed Apr 18 14:01:38 2012 +0200

    refactored API for configuring the serializer

    * [get|set]Objects* => [get|set]Serializer
     * added support to set a callback to configure the serializer
     * added support for app level serialization groups setting
     * moved serializer configuration code from ViewHandler::createResponse() to ViewHandler::getSerializer()