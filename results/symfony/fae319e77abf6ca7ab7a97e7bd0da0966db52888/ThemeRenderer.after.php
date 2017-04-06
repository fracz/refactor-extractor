<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Component\Form\Renderer;

use Symfony\Component\Form\FormInterface;
use Symfony\Component\Form\Renderer\Theme\FormThemeInterface;
use Symfony\Component\Form\Renderer\Theme\FormThemeFactoryInterface;

class ThemeRenderer implements FormRendererInterface, \ArrayAccess, \IteratorAggregate
{
    private $form;

    private $blockHistory = array();

    private $themeFactory;

    private $theme;

    private $vars = array();

    /**
     * Is the form attached to this renderer rendered?
     *
     * Rendering happens when either the widget or the row method was called.
     * Row implicitly includes widget, however certain rendering mechanisms
     * have to skip widget rendering when a row is rendered.
     *
     * @var Boolean
     */
    private $rendered = false;

    private $parent;

    private $children = array();

    public function __construct(FormThemeFactoryInterface $themeFactory, $template = null)
    {
        $this->themeFactory = $themeFactory;

        $this->setTemplate($template);
    }

    public function setParent(self $parent)
    {
        $this->parent = $parent;
    }

    public function setChildren(array $children)
    {
        $this->children = $children;
    }

    public function setTemplate($template)
    {
        $this->setTheme($this->themeFactory->create($template));
    }

    public function setTheme(FormThemeInterface $theme)
    {
        $this->theme = $theme;
    }

    public function getTheme()
    {
        return $this->theme;
    }

    public function setBlock($block)
    {
        array_unshift($this->blockHistory, $block);
    }

    public function getBlock()
    {
        reset($this->blockHistory);

        return current($this->block);
    }

    public function setVar($name, $value)
    {
        $this->vars[$name] = $value;
    }

    public function setAttribute($name, $value)
    {
        $this->vars['attr'][$name] = $value;
    }

    public function hasVar($name)
    {
        return array_key_exists($name, $this->vars);
    }

    public function getVar($name)
    {
        if (!isset($this->vars[$name])) {
            return null;
        }

        return $this->vars[$name];
    }

    public function getVars()
    {
        return $this->vars;
    }

    public function isRendered()
    {
        return $this->rendered;
    }

    public function getWidget(array $vars = array())
    {
        $this->rendered = true;

        return $this->render('widget', $vars);
    }

    public function getErrors(array $vars = array())
    {
        return $this->render('errors', $vars);
    }

    public function getRow(array $vars = array())
    {
        $this->rendered = true;

        return $this->render('row', $vars);
    }

    public function getRest(array $vars = array())
    {
        return $this->render('rest', $vars);
    }

    /**
     * Renders the label of the given form
     *
     * @param FormInterface $form  The form to render the label for
     * @param array $params          Additional variables passed to the block
     */
    public function getLabel($label = null, array $vars = array())
    {
        if (null !== $label) {
            $vars['label'] = $label;
        }

        return $this->render('label', $vars);
    }

    public function getEnctype()
    {
        return $this->render('enctype', $this->vars);
    }

    protected function render($part, array $vars = array())
    {
        return $this->theme->render($this->blockHistory, $part, array_replace(
            $this->vars,
            $vars
        ));
    }

    public function getParent()
    {
        return $this->parent;
    }

    public function hasParent()
    {
        return null !== $this->parent;
    }

    public function getChildren()
    {
        return $this->children;
    }

    public function hasChildren()
    {
        return count($this->children) > 0;
    }

    public function offsetGet($name)
    {
        return $this->children[$name];
    }

    public function offsetExists($name)
    {
        return isset($this->children[$name]);
    }

    public function offsetSet($name, $value)
    {
        throw new \BadMethodCallException('Not supported');
    }

    public function offsetUnset($name)
    {
        throw new \BadMethodCallException('Not supported');
    }

    public function getIterator()
    {
        if (isset($this->children)) {
            $this->rendered = true;

            return new \ArrayIterator($this->children);
        }

        return new \ArrayIterator(array());
    }
}