commit ee24c79aeb0c354e431562447d5731fcf9bbb7f2
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Mar 5 19:37:24 2012 +0100

    Added basic support for passing the command line options when running builds via the tooling api. This feature significantly improves the usability of the tooling api. Also it has a hidden benefit of adding a way of integ-testing the custom plugins via an official API. Some details:

    -parsing happens in the provider implementation. Obviously, the outcome may depend on the target gradle version used.
    -added integ test coverage for some cases, will add much more, including the unhappy paths, etc.
    -the information about the args is carried in the operation parameters because in theory the model building activity might be interested in that at some point.

    Pending:

    -more coverage
    -support log level changing
    -certain options can be also configured on the connector - decide what's more important and agree on desired behavior.