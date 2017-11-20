commit cc53a1d085267a36599eeb4f1e11c01ddbbea553
Author: Johannes Edmeier <johannes.edmeier@gmail.com>
Date:   Mon Oct 3 18:42:06 2016 +0200

    Improve handling for hystrix

     * Since the hystrix.stream incorrectly handles HEAD requests we had to change
       the detection for the hystrix-endpoint to a GET request.
       Issue https://github.com/Netflix/Hystrix/issues/1369

     * We need to improve Zuul's SimpleHostRoutingFilter and SendResponseFilter to
       close the underlying socket when an error occurs when writing the response.
       I hope that the code can be removen when my PR gets merged.
       Issue https://github.com/spring-cloud/spring-cloud-netflix/pull/1372

    Fixes #290