commit 3d8169baf8ff09bc28c655108d4ed9609397633f
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Oct 30 16:27:21 2012 +0100

    Improving TestNG reports. First, some refactoring.

    1. Extracted the JUnit xml report serialization from the JUnitXmlReportGenerator so that I can reuse it for TestNG. I'd rather avoid reusing parts of StateTrackingTestResultProcessor at this time.
    2. Tightened the integration tests a little bit. Some new methods in the fixture code are not yet used but they will be needed soon (it was easier to include it now).