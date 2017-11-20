commit b94af3fa916f3c9a090c9499e35cddc677777680
Author: Sami Salonen <ssalonen@gmail.com>
Date:   Wed Oct 12 04:52:19 2016 +0300

    Persistence service implementation for AWS DynamoDB (#4542)

    * Initial commit of dynamodb binding

    * working store&query

    * cleanup

    * using constants

    * removing obsolete stuff

    * interface

    * consistent naming

    * wip

    * tests, bug fixes

    * more clear test

    * rest of the tests

    * formatting

    * rename

    * skip integration tests if no properties specified

    * accidental smarthome import

    * compare actual states

    * cleanup

    * Use round instead of setScale

    * Copyright notice

    * more strict comparison of bigdecimals

    * less verbose logging

    * README

    * Method renaming

    * test comments

    * rm readme

    * Timezone bug fix (local vs UTC)

    * More tests with datetimetypes having local and utc timezones

    * Remove local sourcepath from dynamodb .classpath

    * PR 4542 code review: @return javadoc

    * PR 4542 code review: AbstractTwoItemIntegrationTest improved javadoc for the class

    * PR 4542 code review: clarify switch item and contact item test comments

    * PR 4542 code review: AbstractDynamoDBItem getter/setter design choices documented better

    * PR 4542 code review: DynamoDBClient java doc fix

    * PR 4542 code review: Using parameterized logging. Improved log message when region is invalid.

    * PR 4542 code review: Service activated log message is now the last statement

    * PR 4542 code review: DynamoDBPersistenceService now tries connection again on later store/query even if first connection was unsucessfull

    * PR 4542 code review: log message cleanup

    * Cleanly catch dynamodb query exceptions

    * better error messages on different connection errors.

    * catch-all exception handling when testing connection

    * @author added

    * PR 4542 code review: java doc correction (dynamodb is not a time series db)

    * PR 4542 code review: cleaner handling of configuration creation. Null & empty service configuration now explicitly handled

    * java7 compat fix