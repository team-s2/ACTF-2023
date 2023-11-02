<?php

use Twig\Environment;
use Twig\Error\LoaderError;
use Twig\Error\RuntimeError;
use Twig\Extension\SandboxExtension;
use Twig\Markup;
use Twig\Sandbox\SecurityError;
use Twig\Sandbox\SecurityNotAllowedTagError;
use Twig\Sandbox\SecurityNotAllowedFilterError;
use Twig\Sandbox\SecurityNotAllowedFunctionError;
use Twig\Source;
use Twig\Template;

/* 500 */
class __TwigTemplate_6ab27504cd80be73207a8f6d9d1e6e5f9e06addd48a5bf73e070e2232b269dfb extends Template
{
    private $source;
    private $macros = [];

    public function __construct(Environment $env)
    {
        parent::__construct($env);

        $this->source = $this->getSourceContext();

        $this->blocks = [
            'message' => [$this, 'block_message'],
        ];
    }

    protected function doGetParent(array $context)
    {
        // line 1
        return "_layouts/message";
    }

    protected function doDisplay(array $context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("template", "500");
        // line 2
        $context["title"] = $this->extensions['craft\web\twig\Extension']->translateFilter("Internal Server Error", "app");
        // line 1
        $this->parent = $this->loadTemplate("_layouts/message", "500", 1);
        $this->parent->display($context, array_merge($this->blocks, $blocks));
        craft\helpers\Template::endProfile("template", "500");
    }

    // line 4
    public function block_message($context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("block", "message");
        // line 5
        echo "    <h2>";
        echo twig_escape_filter($this->env, ($context["title"] ?? null), "html", null, true);
        echo "</h2>
    <p>";
        // line 6
        echo twig_escape_filter($this->env, (((($context["message"]) ?? (null))) ? ((($context["message"]) ?? (null))) : ($this->extensions['craft\web\twig\Extension']->translateFilter("An error occurred while processing your request.", "app"))), "html", null, true);
        echo "</p>
";
        craft\helpers\Template::endProfile("block", "message");
    }

    public function getTemplateName()
    {
        return "500";
    }

    public function isTraitable()
    {
        return false;
    }

    public function getDebugInfo()
    {
        return array (  61 => 6,  56 => 5,  51 => 4,  45 => 1,  43 => 2,  35 => 1,);
    }

    public function getSourceContext()
    {
        return new Source("", "500", "/var/www/html/vendor/craftcms/cms/src/templates/500.twig");
    }
}
