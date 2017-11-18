commit decb6329dba834043d72c44457d997d0409eda4f
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Wed Oct 4 04:50:11 2017 -0400

    DefaultWebTestClient internal refactoring

    Use the ClientResponse methods bodyToMono and bodyToFlux rather than
    passing in a BodyExtractor so that WebTestclient now also benefits from
    the recently improved handling of Void.class.