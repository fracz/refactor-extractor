commit cddb23a8919805b754ac09cc957d5f914273b8b0
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Dec 22 21:01:20 2011 +0100

    Tooling api serves gradle version even for old providers...

    Since internally we always had information about gradle version (connection.metadata) I was able to implement a short-circuit in the consumer to server this information even for old providers, like M3 or M4. This will be very valuable to tooling api clients like STS because they can safely assume gradle version is always provided by the tooling api, regardless of the provider version in use.

    -reduced some duplication around exceptions
    -added integ coverage + some refactorings