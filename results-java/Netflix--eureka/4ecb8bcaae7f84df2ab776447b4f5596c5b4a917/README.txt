commit 4ecb8bcaae7f84df2ab776447b4f5596c5b4a917
Author: keithbranton <keithbranton@overstock.com>
Date:   Thu Oct 30 11:06:08 2014 -0600

    Forgot to use ProxyCustomApacheHttpClientConfig to provide the client
    configuration in EurekaJerseyClient.createProxyJerseyClient when I
    initially refactored this.