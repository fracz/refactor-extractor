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
use Symfony\Component\Form\Renderer\Theme\ThemeInterface;
use Symfony\Component\Form\Renderer\Plugin\RendererPluginInterface;

class DefaultRenderer implements RendererInterface, \ArrayAccess
{
    private $field;

    private $template;

    private $theme;

    private $vars = array();

    private $changes = array();

    private $initialized = false;

    /**
     * Is the field attached to this renderer rendered?
     *
     * Rendering happens when either the widget or the row method was called.
     * Row implicitly includes widget, however certain rendering mechanisms
     * have to skip widget rendering when a row is rendered.
     *
     * @var bool
     */
    private $rendered = false;

    private $children = array();

    public function __construct(ThemeInterface $theme, $template)
    {
        $this->theme = $theme;
        $this->template = $template;
    }

    public function __clone()
    {
        foreach ($this->changes as $key => $change) {
            if (is_object($change)) {
                $this->changes[$key] = clone $change;
            }
        }
    }

    private function initialize()
    {
        if (!$this->initialized) {
            $this->initialized = true;

            // Make sure that plugins and set variables are applied in the
            // order they were added
            foreach ($this->changes as $key => $value) {
                if ($value instanceof RendererPluginInterface) {
                    $value->setUp($this->field, $this);
                } else {
                    $this->vars[$key] = $value;
                }
            }

            $this->changes = array();
        }
    }

    public function setField(FormInterface $field)
    {
        $this->field = $field;
    }

    public function setChildren(array $renderers)
    {
        $this->children = $renderers;
    }

    public function setTheme(ThemeInterface $theme)
    {
        $this->theme = $theme;
    }

    public function getTheme()
    {
        return $this->theme;
    }

    public function addPlugin(RendererPluginInterface $plugin)
    {
        $this->initialized = false;
        $this->changes[] = $plugin;
    }

    public function setVar($name, $value)
    {
        if ($this->initialized) {
            $this->vars[$name] = $value;
        } else {
            $this->changes[$name] = $value;
        }
    }

    public function hasVar($name)
    {
        return array_key_exists($name, $this->vars);
    }

    public function getVar($name)
    {
        $this->initialize();

        // TODO exception handling
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
     * Renders the label of the given field
     *
     * @param FormInterface $field  The field to render the label for
     * @param array $params          Additional variables passed to the template
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

    protected function render($block, array $vars = array())
    {
        $this->initialize();

        return $this->theme->render($this->template, $block, array_replace(
            $this->vars,
            $vars
        ));
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
}