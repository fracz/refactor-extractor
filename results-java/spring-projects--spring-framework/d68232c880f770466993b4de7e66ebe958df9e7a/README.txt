commit d68232c880f770466993b4de7e66ebe958df9e7a
Author: Violeta Georgieva <vgeorgieva@pivotal.io>
Date:   Thu Jul 7 15:21:58 2016 +0300

    Refactor AbstractRequestBodyPublisher states

    The state machine is refactored in order to solve various concurrency
    issues.