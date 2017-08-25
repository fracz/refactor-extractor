commit c8596dce5cd123931afb69d70ed3d342f49c8048
Author: Alf Henderson <alfhenderson@gmail.com>
Date:   Wed Apr 6 08:43:34 2016 +0200

    Refactored getTranslation() for readability and performance improvement

    The  condition logic in getTranlsation() was hard to read and slow on
    perfomance as it was calling the methods twice for each if() condition:

    ###### Sample

    ``` php

    // If condition in getTranslation() before refactor
    // First we are getting the value in the if() statement, then if it
    returns truthy
    // We get the value again inside the condition.

    if ($this->getTranslationByLocaleKey($locale)) {
        $translation = $this->getTranslationByLocaleKey($locale);
    }

    // This is slow because we run the exact same logic twice without
    needing to

    // Refactored version
    if ($translation = $this->getTranslationByLocaleKey($locale)) {
        return $translation;
    }

    ```

    I measured the execution of the function using a Microtime timer:

    Execution time before improvement: 0.0040078163146973

    Execution time after improvement:  0.0028159618377686

    As seen this roughly amounts to a 25% performance enhancement.