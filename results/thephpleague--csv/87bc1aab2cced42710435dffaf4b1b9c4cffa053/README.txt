commit 87bc1aab2cced42710435dffaf4b1b9c4cffa053
Author: ignace nyamagana butera <nyamsprod@gmail.com>
Date:   Mon Feb 27 09:34:39 2017 +0100

    Implementing 9.0 Public API

    The goals are

    - to make the package more SOLID
    - to improve stream filter usage

    ### Added

    #### Classes

    - `League\Csv\Statement` : a Constraint builder class
    - `League\Csv\RecordSet` : a class to manipulare CSV document records
    - `League\Csv\Exception\CsvException` : an interface implemented by all package exceptions
    - `League\Csv\Exception\InvalidArgumentException`
    - `League\Csv\Exception\RuntimeException`
    - `League\Csv\Exception\InsertionException`

    ### Methods

    - `AbstractCsv::isStream` tell whether Stream filtering is supported
    - `AbstractCsv::addStreamFilter` append a new Stream filter to the CSV document
    - `Reader::getHeaderOffset` returns the CSV document header record offset
    - `Reader::getHeader` returns the CSV document header record
    - `Reader::select` returns a RecordSet object
    - `Reader::setHeaderOffset` sets the CSV document header record offset
    - `Writer::getFlushThreshold` returns the flushing mechanism threshold
    - `Writer::setFlushThreshold` sets the flushing mechanism threshold

    ### Fixed

    - Stream filtering is simplified and is supported for every object except when created from `SplFileObject`.
    - `Writer::insertOne` returns the numbers of bytes added
    - `Writer::insertOne` throws an exception on error instead of failing silently
    - `Writer::insertAll` returns the numbers of bytes added
    - `AbstractCsv::output` now HTTP/2 compliant
    - `AbstractCsv` are no longer clonable.

    ### Deprecated

    - None

    ### Removed

    - `AbstractCsv::newReader`
    - `AbstractCsv::newWriter`
    - `AbstractCsv::isActiveStreamFilter` replaced by `AbstractCsv::isStream`
    - `AbstractCsv::appendStreamFilter` replaced by `Reader::addStreamFilter`
    - `AbstractCsv::prependStreamFilter` replaced by `Reader::addStreamFilter`
    - `InvalidRowException` replaced by `InsertionException`
    - `Writer::fetchDelimitersOccurence`
    - `Writer::getIterator`
    - `Writer::jsonSerialize`
    - `Writer::toXML`
    - `Writer::toHTML`
    - `Reader::getNewLine`
    - `Reader::setNewLine`
    - `Reader::fetchAll` replaced by `RecordSet::fetchAll`
    - `Reader::fetchOne` replaced by `RecordSet::fetchOne`
    - `Reader::fetchColumn` replaced by `RecordSet::fetchColumn`
    - `Reader::fetchPairs` replaced by `RecordSet::fetchPairs`
    - `Reader::toXML` replaced by `RecordSet::toXML`
    - `Reader::toHTML` replaced by `RecordSet::toHTML`

    ### Internal changes

    - Improved `StreamIterator`
    - Removed the `League\Csv\Config` namespace
    - `AbstractCsv::createFromString` now uses `StreamIterator` instead of `SplFileObject`
    - `AbstractCsv::createFromPath` now uses `StreamIterator` instead of `SplFileObject`