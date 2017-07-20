<?php

namespace CachetHQ\Cachet\Http\After;

use CachetHQ\Cachet\Models\Setting;
use Illuminate\Http\Request;
use Illuminate\Routing\Route;
use Symfony\Component\HttpFoundation\Response;

class AllowedDomainsFilter
{
    public function filter(Route $route, Request $request, Response $response)
    {
        // Always allow our own domain.
        $ourDomain = Setting::get('app_domain');
        $response->headers->set('Access-Control-Allow-Origin', $ourDomain);

        // Should we allow anyone else?
        if ($setting = Setting::get('allowed_domains')) {
            $domains = explode(',', $setting);
            foreach ($domains as $domain) {
                $response->headers->set('Access-Control-Allow-Origin', $domain);
            }
        }

        return $response;
    }
}