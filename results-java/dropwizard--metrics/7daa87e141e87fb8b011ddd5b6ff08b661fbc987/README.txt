commit 7daa87e141e87fb8b011ddd5b6ff08b661fbc987
Author: Mårten Gustafson <marten.gustafson@gmail.com>
Date:   Sun Nov 20 10:58:40 2011 +0100

    Make polling reporters testable and use the same approach when testing
    them. Applies to:
    - CsvReporter and ConsoleReporter (metrics-core)
    - GangliaReporter (metrics-ganglia)
    - GraphiteReporter (metrics-graphite)

    Besides the refactorings done to be able to test the reporters this
    change also makes metrics-core build a test-jar so that other modules
    can include that as a test dependency (see for example
    metrics-graphite/pom.xml) in addition to them having a regular compile
    dependency on metrics-core. This change allows the modules to access the
    test classes in metrics-core and hence we're able to use the same
    abstract base class (AbstractPollingReporterTest) for all polling reporter tests.