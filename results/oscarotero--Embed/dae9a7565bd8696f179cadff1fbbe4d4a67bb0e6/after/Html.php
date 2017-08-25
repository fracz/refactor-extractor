<?php
namespace Embed\Providers;

use Embed\Url;
use Embed\Bag;
use Embed\Request;
use Embed\Utils;

/**
 * Generic html provider.
 *
 * Load the html data of an url and store it
 */
class Html extends Provider implements ProviderInterface
{
    /**
     * {@inheritdoc}
     */
    public function init(Request $request, array $options)
    {
        if (!($html = $request->getHtmlContent())) {
            return false;
        }

        self::extractFromLink($html, $this->bag);
        self::extractFromMeta($html, $this->bag);

        $main = self::getMainElement($html);

        self::extractImages($main, $this->bag, $request->url->getDomain());

        //Title
        $title = $html->getElementsByTagName('title');

        if ($title->length) {
            $this->bag->set('title', $title->item(0)->nodeValue);
        }



        //Time
        /*
        foreach($html->getElementsByTagName('time') as $time) {
            if ($time->hasAttribute('itemprop')) {
                $name = strtolower($time->getAttribute('itemprop'));
                $this->set($name, $time->getAttribute('datetime'));
            } else if ($time->hasAttribute('datetime')) {
                $this->set('datepublished', $time->getAttribute('datetime'));
            }
        }
        */
    }

    /**
     * {@inheritdoc}
     */
    public function getTitle()
    {
        return $this->bag->get('title');
    }

    /**
     * {@inheritdoc}
     */
    public function getDescription()
    {
        return $this->bag->get('description');
    }

    /**
     * {@inheritdoc}
     */
    public function getType()
    {
        return $this->bag->has('video_src') ? 'video' : null;
    }

    /**
     * {@inheritdoc}
     */
    public function getSource()
    {
        $feeds = $this->bag->get('feeds');

        return isset($feeds[0]) ? $feeds[0] : null;
    }

    /**
     * {@inheritdoc}
     */
    public function getCode()
    {
        if ($this->bag->has('video_src')) {
            switch ($this->bag->get('video_type')) {
                case 'application/x-shockwave-flash':
                    return Utils::flash($this->bag->get('video_src'), $this->getWidth(), $this->getHeight());
            }
        }
    }

    /**
     * {@inheritdoc}
     */
    public function getUrl()
    {
        return $this->bag->get('canonical');
    }

    /**
     * {@inheritdoc}
     */
    public function getProviderIcons()
    {
        return $this->bag->get('icons');
    }

    /**
     * {@inheritdoc}
     */
    public function getImages()
    {
        return $this->bag->get('images') ?: array();
    }

    /**
     * {@inheritdoc}
     */
    public function getWidth()
    {
        return $this->bag->get('video_width');
    }

    /**
     * {@inheritdoc}
     */
    public function getHeight()
    {
        return $this->bag->get('video_height');
    }

    /**
     * {@inheritdoc}
     */
    public function getPublishedTime()
    {
        return $this->bag->get('pub_date')
            ?: $this->bag->get('date')
            ?: $this->bag->get('pagerender')
            ?: $this->bag->get('datepublished');
    }

    /**
     * Extract information from the <link> elements
     *
     * @param \DOMDocument $html
     * @param Bag          $bag
     */
    protected static function extractFromLink(\DOMDocument $html, Bag $bag)
    {
        foreach (Utils::getLinks($html) as $link) {
            list($rel, $href, $element) = $link;

            if (empty($href)) {
                continue;
            }

            switch ($rel) {
                case 'favicon':
                case 'favico':
                case 'icon':
                case 'shortcut icon':
                case 'apple-touch-icon-precomposed':
                case 'apple-touch-icon':
                    $bag->add('icons', $href);
                    break;

                case 'image_src':
                    $bag->add('images', $href);
                    break;

                case 'alternate':
                    switch ($element->getAttribute('type')) {
                        case 'application/rss+xml':
                        case 'application/atom+xml':
                            $bag->add('feeds', $href);
                            break;
                    }
                    break;

                default:
                    $bag->set($rel, $href);
            }
        }
    }

    /**
     * Extract information from the <meta> elements
     *
     * @param \DOMDocument $html
     * @param Bag          $bag
     */
    protected static function extractFromMeta(\DOMDocument $html, Bag $bag)
    {
        foreach (Utils::getMetas($html) as $meta) {
            list($name, $value, $element) = $meta;

            if (!$value) {
                continue;
            }

            if ($name) {
                $name = strtolower($name);

                switch ($name) {
                    case 'msapplication-tileimage':
                        $bag->add('icons', $value);
                        continue 2;

                    default:
                        $bag->set($name, $value);
                        continue 2;
                }
            }

            if ($element->hasAttribute('itemprop')) {
                $bag->set($element->getAttribute('itemprop'), $value);
            }

            if ($element->hasAttribute('http-equiv')) {
                $bag->set($element->getAttribute('http-equiv'), $value);
            }
        }
    }

    /**
     * Extract <img> elements
     *
     * @param \DOMElement  $html
     * @param Bag          $bag
     * @param null|string  $domain
     */
    protected static function extractImages(\DOMElement $html, Bag $bag, $domain = null)
    {
        foreach ($html->getElementsByTagName('img') as $img) {
            if ($img->hasAttribute('src')) {
                $src = new Url($img->getAttribute('src'));

                //Is src relative?
                if (!$src->getDomain()) {
                    $bag->add('images', $src->getUrl());
                    continue;
                }

                //Avoid external images or in external links
                if ($domain) {
                    if ($src->getDomain() !== $domain) {
                        continue;
                    }

                    $parent = $img->parentNode;

                    while ($parent && isset($parent->tagName)) {
                        if ($parent->tagName === 'a') {
                            if ($parent->hasAttribute('href')) {
                                $href = new Url($parent->getAttribute('href'));

                                if ($href->getDomain() && $src->getDomain() !== $domain) {
                                    continue 2;
                                }
                            }

                            break;
                        }

                        $parent = $parent->parentNode;
                    }

                    $bag->add('images', $src->getUrl());
                }
            }
        }
    }

    /**
     * Returns the main element of the document
     *
     * @param \DOMDocument $html
     *
     * @return \DOMElement
     */
    protected static function getMainElement(\DOMDocument $html)
    {
        // <main>
        $content = $html->getElementsByTagName('main');

        if ($content->length !== 0) {
            return $content->item(0);
        }

        // Popular ids: #main, #content, #page
        $content = $html->getElementById('main') ?: $html->getElementById('content') ?: $html->getElementById('page');

        if ($content) {
            return $content;
        }

        // Wordpress ids: #post-*
        foreach ($html->getElementsByTagName('article') as $article) {
            if ($article->hasAttribute('id') && (strpos($article->getAttribute('id'), 'post-') === 0)) {
                return $article;
            }
        }

        // Returns <body>
        return $html->getElementsByTagName('body')->item(0);
    }
}