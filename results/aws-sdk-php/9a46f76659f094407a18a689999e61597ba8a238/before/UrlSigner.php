<?php
namespace Aws\CloudFront;

use GuzzleHttp\Query;
use GuzzleHttp\Url;

/**
 * Creates signed URLs for Amazon CloudFront resources.
 */
class UrlSigner
{
    private $keyPairId;
    private $pk;

    /**
     * @param $keyPairId  string ID of the key pair
     * @param $privateKey string Path to the private key used for signing
     *
     * @throws \RuntimeException if the openssl extension is missing
     * @throws \InvalidArgumentException if the private key cannot be found.
     */
    public function __construct($keyPairId, $privateKey)
    {
        if (!extension_loaded('openssl')) {
            //@codeCoverageIgnoreStart
            throw new \RuntimeException('The openssl extension is required to '
                . 'sign CloudFront urls.');
            //@codeCoverageIgnoreEnd
        }

        $this->keyPairId = $keyPairId;

        if (!file_exists($privateKey)) {
            throw new \InvalidArgumentException("PK file not found: $privateKey");
        }

        $this->pk = file_get_contents($privateKey);
    }

    /**
     * Create a signed Amazon CloudFront URL.
     *
     * Keep in mind that URLs meant for use in media/flash players may have
     * different requirements for URL formats (e.g. some require that the
     * extension be removed, some require the file name to be prefixed
     * - mp4:<path>, some require you to add "/cfx/st" into your URL).
     *
     * @param string              $url     URL to sign (can include query
     *                                     string string and wildcards
     * @param string|integer|null $expires UTC Unix timestamp used when signing
     *                                     with a canned policy. Not required
     *                                     when passing a custom $policy.
     * @param string              $policy  JSON policy. Use this option when
     *                                     creating a signed URL for a custom
     *                                     policy.
     *
     * @return string The file URL with authentication parameters
     * @throws \InvalidArgumentException if the URL provided is invalid
     * @link http://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/WorkingWithStreamingDistributions.html
     */
    public function getUrlSigner($url, $expires = null, $policy = null)
    {
        // Determine the scheme of the url
        $urlSections = explode('://', $url);

        if (count($urlSections) < 2) {
            throw new \InvalidArgumentException("Invalid URL: {$url}");
        }

        // Get the real scheme by removing wildcards from the scheme
        $scheme = str_replace('*', '', $urlSections[0]);

        if ($policy) {
            $isCustom = true;
        } else {
            $isCustom = false;
            $policy = $this->createCannedPolicy($scheme, $url, $expires);
        }

        $policy = str_replace(' ', '', $policy);
        $url = Url::fromString($scheme . '://' . $urlSections[1]);
        $this->prepareQuery($isCustom, $policy, $url->getQuery(), $expires);

        return $scheme === 'rtmp'
            ? $this->createRtmpUrl($url)
            : (string) $url;
    }

    private function prepareQuery($isCustom, $policy, Query $query, $expires)
    {
        if ($isCustom) {
            // Custom policies require the encoded policy be specified in query
            $query['Policy'] = strtr(base64_encode($policy), '+=/', '-_~');
        } else {
            // Canned policies require "Expires" in the URL.
            $query['Expires'] = $expires;
        }

        $query['Signature'] = $this->signPolicy($policy);
        $query['Key-Pair-Id'] = $this->keyPairId;
    }

    private function rsaSha1Sign($policy)
    {
        $signature = '';
        openssl_sign($policy, $signature, $this->pk);

        return $signature;
    }

    private function createCannedPolicy($scheme, $url, $expires)
    {
        if (!$expires) {
            throw new \InvalidArgumentException('An expires option is '
                . 'required when using a canned policy');
        }

        return sprintf(
            '{"Statement":[{"Resource":"%s","Condition":{"DateLessThan":{"AWS:EpochTime":%d}}}]}',
            $this->createResource($scheme, $url),
            $expires
        );
    }

    private function signPolicy($policy)
    {
        // Sign the policy using the CloudFront private key
        $signedPolicy = $this->rsaSha1Sign($policy);
        // Remove whitespace, base64 encode the policy, and replace special
        // characters.
        $signedPolicy = strtr(base64_encode($signedPolicy), '+=/', '-_~');

        return $signedPolicy;
    }

    private function createResource($scheme, $url)
    {
        switch ($scheme) {
            case 'http':
            case 'https':
                return $url;
            case 'rtmp':
                $parts = parse_url($url);
                $pathParts = pathinfo($parts['path']);
                $resource = ltrim(
                    $pathParts['dirname'] . '/' . $pathParts['basename'],
                    '/'
                );

                // Add a query string if present.
                if (isset($parts['query'])) {
                    $resource .= "?{$parts['query']}";
                }

                return $resource;
        }

        throw new \InvalidArgumentException("Invalid URI scheme: {$scheme}. "
            . "Scheme must be one of: http, https, or rtmp");
    }

    private function createRtmpUrl(Url $url)
    {
        // Use a relative URL when creating Flash player URLs
        $url->getQuery()->setEncodingType(false);
        $url->setScheme(null);
        $url->setHost(null);

        return substr($url, 1);
    }
}