commit 9725329c803b0e949ca92f573c064eb98f050916
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Mon Feb 23 21:12:30 2015 +0100

    QuickImport utility

    for just getting a dataset of size X imported as quickly as possible. Uses
    CsvDataGenerator for generating very crude, random data of varying sizes.
    Focuses on size more than layout of the data.

    QuickImport uses CsvDataGenerator as Input to BatchImporter for
    short-circuting the data generator and importer.

    Made refactorings around InputEntityDeserializer (CharSeeker->InputEntity)
    to allow for composition and reuse of code for this purpose.