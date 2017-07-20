commit a615b2ad79e86a41371a106499fb1ec833bd080d
Author: George Kotchlamazashvili <georgedot@gmail.com>
Date:   Sat May 14 05:51:35 2016 +0400

    ka_GE: overall improvements to ka_GE locale

    General improvemens were made to existing Provides as well
    as added Company and Lorem providers (generators)

    ka_GE\Address:
    * trimed items in arrays
    * added multiple address formats (full, short and etc.)
    * added cityFormats
    * added regionGenitiveForm (gramar)

    ka_GE\Company:
    * generates names using popular name elements with affixes

    ka_GE\Internet:
    * added items to freeEmailDomain array
    * added all supported Georgian TLDs

    ka_GE\Lorem:
    * added about 300 localized lorem words

    ka_GE\Person:
    * added titles for genders