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

/* _layouts/message */
class __TwigTemplate_24cf5e56d4f2705c5b85f3b135b422b02d87888534e4062081e959e1b75bee14 extends Template
{
    private $source;
    private $macros = [];

    public function __construct(Environment $env)
    {
        parent::__construct($env);

        $this->source = $this->getSourceContext();

        $this->blocks = [
            'body' => [$this, 'block_body'],
            'message' => [$this, 'block_message'],
            'foot' => [$this, 'block_foot'],
        ];
    }

    protected function doGetParent(array $context)
    {
        // line 1
        return "_layouts/base";
    }

    protected function doDisplay(array $context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("template", "_layouts/message");
        // line 2
        $context["bodyClass"] = "message";
        // line 1
        $this->parent = $this->loadTemplate("_layouts/base", "_layouts/message", 1);
        $this->parent->display($context, array_merge($this->blocks, $blocks));
        craft\helpers\Template::endProfile("template", "_layouts/message");
    }

    // line 4
    public function block_body($context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("block", "body");
        // line 5
        echo "    <div class=\"message-container\">
        <div id=\"message\" class=\"pane\">
            ";
        // line 7
        $this->displayBlock('message', $context, $blocks);
        // line 8
        echo "        </div>
    </div>
";
        craft\helpers\Template::endProfile("block", "body");
    }

    // line 7
    public function block_message($context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("block", "message");
        craft\helpers\Template::endProfile("block", "message");
    }

    // line 12
    public function block_foot($context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("block", "foot");
        // line 13
        echo "    <script type=\"text/javascript\">
        var message = document.getElementById('message'),
            margin = -Math.round(message.offsetHeight / 2);
        message.setAttribute('style', 'margin-top: '+margin+'px !important;');
    </script>
";
        craft\helpers\Template::endProfile("block", "foot");
    }

    public function getTemplateName()
    {
        return "_layouts/message";
    }

    public function isTraitable()
    {
        return false;
    }

    public function getDebugInfo()
    {
        return array (  84 => 13,  79 => 12,  71 => 7,  64 => 8,  62 => 7,  58 => 5,  53 => 4,  47 => 1,  45 => 2,  37 => 1,);
    }

    public function getSourceContext()
    {
        return new Source("", "_layouts/message", "/var/www/html/vendor/craftcms/cms/src/templates/_layouts/message.twig");
    }
}
