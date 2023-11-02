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

/* _layouts/base */
class __TwigTemplate_0dab7b8106ee2f5630fc43de84671a2b495a78df92902de24725041e65a3a494 extends Template
{
    private $source;
    private $macros = [];

    public function __construct(Environment $env)
    {
        parent::__construct($env);

        $this->source = $this->getSourceContext();

        $this->parent = false;

        $this->blocks = [
            'head' => [$this, 'block_head'],
            'body' => [$this, 'block_body'],
            'foot' => [$this, 'block_foot'],
        ];
    }

    protected function doDisplay(array $context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("template", "_layouts/base");
        // line 1
        $context["systemName"] = $this->extensions['craft\web\twig\Extension']->translateFilter(craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, ($context["craft"] ?? null), "app", []), "getSystemName", [], "method"), "site");
        // line 2
        $context["docTitle"] = ((array_key_exists("docTitle", $context)) ? (($context["docTitle"] ?? null)) : (twig_striptags(($context["title"] ?? null))));
        // line 3
        $context["orientation"] = craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, ($context["craft"] ?? null), "app", []), "locale", []), "getOrientation", [], "method");
        // line 4
        $context["a11yDefaults"] = craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, ($context["craft"] ?? null), "app", []), "config", []), "general", []), "accessibilityDefaults", []);
        // line 5
        $context["requestedSite"] = craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, ($context["craft"] ?? null), "cp", []), "requestedSite", []);
        // line 6
        echo "
";
        // line 7
        $context["bodyClass"] = $this->extensions['craft\web\twig\Extension']->filterFilter($this->env, $this->extensions['craft\web\twig\Extension']->mergeFilter(craft\helpers\Html::explodeClass((($context["bodyClass"]) ?? ([]))), [0 =>         // line 8
($context["orientation"] ?? null), 1 => (( !(((craft\helpers\Template::attribute($this->env, $this->source,         // line 9
($context["currentUser"] ?? null), "getPreference", [0 => "alwaysShowFocusRings"], "method", true, true) &&  !(null === craft\helpers\Template::attribute($this->env, $this->source, ($context["currentUser"] ?? null), "getPreference", [0 => "alwaysShowFocusRings"], "method")))) ? (craft\helpers\Template::attribute($this->env, $this->source, ($context["currentUser"] ?? null), "getPreference", [0 => "alwaysShowFocusRings"], "method")) : ((((craft\helpers\Template::attribute($this->env, $this->source, ($context["a11yDefaults"] ?? null), "alwaysShowFocusRings", [], "array", true, true) &&  !(null === (($__internal_compile_0 = ($context["a11yDefaults"] ?? null)) && is_array($__internal_compile_0) || $__internal_compile_0 instanceof ArrayAccess ? ($__internal_compile_0["alwaysShowFocusRings"] ?? null) : null)))) ? ((($__internal_compile_1 = ($context["a11yDefaults"] ?? null)) && is_array($__internal_compile_1) || $__internal_compile_1 instanceof ArrayAccess ? ($__internal_compile_1["alwaysShowFocusRings"] ?? null) : null)) : (false))))) ? ("reduce-focus-visibility") : ("")), 2 => (((((craft\helpers\Template::attribute($this->env, $this->source,         // line 10
($context["currentUser"] ?? null), "getPreference", [0 => "useShapes"], "method", true, true) &&  !(null === craft\helpers\Template::attribute($this->env, $this->source, ($context["currentUser"] ?? null), "getPreference", [0 => "useShapes"], "method")))) ? (craft\helpers\Template::attribute($this->env, $this->source, ($context["currentUser"] ?? null), "getPreference", [0 => "useShapes"], "method")) : ((((craft\helpers\Template::attribute($this->env, $this->source, ($context["a11yDefaults"] ?? null), "useShapes", [], "array", true, true) &&  !(null === (($__internal_compile_2 = ($context["a11yDefaults"] ?? null)) && is_array($__internal_compile_2) || $__internal_compile_2 instanceof ArrayAccess ? ($__internal_compile_2["useShapes"] ?? null) : null)))) ? ((($__internal_compile_3 = ($context["a11yDefaults"] ?? null)) && is_array($__internal_compile_3) || $__internal_compile_3 instanceof ArrayAccess ? ($__internal_compile_3["useShapes"] ?? null) : null)) : (false))))) ? ("use-shapes") : ("")), 3 => (((((craft\helpers\Template::attribute($this->env, $this->source,         // line 11
($context["currentUser"] ?? null), "getPreference", [0 => "underlineLinks"], "method", true, true) &&  !(null === craft\helpers\Template::attribute($this->env, $this->source, ($context["currentUser"] ?? null), "getPreference", [0 => "underlineLinks"], "method")))) ? (craft\helpers\Template::attribute($this->env, $this->source, ($context["currentUser"] ?? null), "getPreference", [0 => "underlineLinks"], "method")) : ((((craft\helpers\Template::attribute($this->env, $this->source, ($context["a11yDefaults"] ?? null), "underlineLinks", [], "array", true, true) &&  !(null === (($__internal_compile_4 = ($context["a11yDefaults"] ?? null)) && is_array($__internal_compile_4) || $__internal_compile_4 instanceof ArrayAccess ? ($__internal_compile_4["underlineLinks"] ?? null) : null)))) ? ((($__internal_compile_5 = ($context["a11yDefaults"] ?? null)) && is_array($__internal_compile_5) || $__internal_compile_5 instanceof ArrayAccess ? ($__internal_compile_5["underlineLinks"] ?? null) : null)) : (false))))) ? ("underline-links") : ("")), 4 => ((        // line 12
($context["requestedSite"] ?? null)) ? (("site--" . craft\helpers\Template::attribute($this->env, $this->source, ($context["requestedSite"] ?? null), "handle", []))) : (""))]));
        // line 15
        $context["bodyAttributes"] = $this->extensions['craft\web\twig\Extension']->mergeFilter(["class" =>         // line 16
($context["bodyClass"] ?? null), "dir" =>         // line 17
($context["orientation"] ?? null)], ((        // line 18
$context["bodyAttributes"]) ?? ([])), true);
        // line 20
        craft\helpers\Template::attribute($this->env, $this->source, ($context["view"] ?? null), "registerAssetBundle", [0 => "craft\\web\\assets\\cp\\CpAsset"], "method");
        // line 21
        $context["cpAssetUrl"] = craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, ($context["view"] ?? null), "getAssetManager", [], "method"), "getPublishedUrl", [0 => "@app/web/assets/cp/dist", 1 => true], "method");
        // line 23
        echo \Craft::$app->getView()->invokeHook("cp.layouts.base", $context);

        // line 25
        echo "<!DOCTYPE html>
<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"";
        // line 26
        echo twig_escape_filter($this->env, craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, ($context["craft"] ?? null), "app", []), "language", []), "html", null, true);
        echo "\">
<head>
    ";
        // line 28
        $this->displayBlock('head', $context, $blocks);
        // line 55
        echo "</head>
<body ";
        // line 56
        echo craft\helpers\Html::renderTagAttributes(($context["bodyAttributes"] ?? null));
        echo ">
    ";
        // line 57
        $this->env->getFunction('beginBody')->getCallable()();
        echo "
    ";
        // line 58
        $this->displayBlock('body', $context, $blocks);
        // line 59
        echo "    ";
        $this->displayBlock('foot', $context, $blocks);
        // line 60
        echo "    ";
        $this->env->getFunction('endBody')->getCallable()();
        echo "
</body>
</html>
";
        craft\helpers\Template::endProfile("template", "_layouts/base");
    }

    // line 28
    public function block_head($context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("block", "head");
        // line 29
        echo "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">
    <meta charset=\"utf-8\">
    <title>";
        // line 31
        echo twig_escape_filter($this->env, ((($context["docTitle"] ?? null) . ((($this->extensions['craft\web\twig\Extension']->lengthFilter($this->env, ($context["docTitle"] ?? null)) && $this->extensions['craft\web\twig\Extension']->lengthFilter($this->env, ($context["systemName"] ?? null)))) ? (" - ") : (""))) . ($context["systemName"] ?? null)), "html", null, true);
        echo "</title>
    ";
        // line 32
        $this->env->getFunction('head')->getCallable()();
        echo "
    <meta name=\"referrer\" content=\"origin-when-cross-origin\">
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">

    ";
        // line 36
        $context["hasCustomIcon"] = false;
        // line 37
        echo "    ";
        $context['_parent'] = $context;
        $context['_seq'] = twig_ensure_traversable(craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, ($context["craft"] ?? null), "app", []), "config", []), "general", []), "cpHeadTags", []));
        foreach ($context['_seq'] as $context["_key"] => $context["tag"]) {
            // line 38
            echo "        ";
            echo $this->extensions['craft\web\twig\Extension']->tagFunction((($__internal_compile_6 = $context["tag"]) && is_array($__internal_compile_6) || $__internal_compile_6 instanceof ArrayAccess ? ($__internal_compile_6[0] ?? null) : null), (($__internal_compile_7 = $context["tag"]) && is_array($__internal_compile_7) || $__internal_compile_7 instanceof ArrayAccess ? ($__internal_compile_7[1] ?? null) : null));
            echo "
        ";
            // line 39
            if ((((($__internal_compile_8 = $context["tag"]) && is_array($__internal_compile_8) || $__internal_compile_8 instanceof ArrayAccess ? ($__internal_compile_8[0] ?? null) : null) == "link") && ((((craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, $context["tag"], 1, [], "array", false, true), "rel", [], "any", true, true) &&  !(null === craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, $context["tag"], 1, [], "array", false, true), "rel", [])))) ? (craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, $context["tag"], 1, [], "array", false, true), "rel", [])) : (null)) == "icon"))) {
                // line 40
                echo "            ";
                $context["hasCustomIcon"] = true;
                // line 41
                echo "        ";
            }
            // line 42
            echo "    ";
        }
        $_parent = $context['_parent'];
        unset($context['_seq'], $context['_iterated'], $context['_key'], $context['tag'], $context['_parent'], $context['loop']);
        $context = array_intersect_key($context, $_parent) + $_parent;
        // line 43
        echo "    ";
        if ( !($context["hasCustomIcon"] ?? null)) {
            // line 44
            echo "        <link rel=\"icon\" href=\"";
            echo twig_escape_filter($this->env, ($context["cpAssetUrl"] ?? null), "html", null, true);
            echo "/images/icons/favicon.ico\">
        <link rel=\"icon\" type=\"image/svg+xml\" sizes=\"any\" href=\"";
            // line 45
            echo twig_escape_filter($this->env, ($context["cpAssetUrl"] ?? null), "html", null, true);
            echo "/images/icons/icon.svg\">
        <link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"";
            // line 46
            echo twig_escape_filter($this->env, ($context["cpAssetUrl"] ?? null), "html", null, true);
            echo "/images/icons/apple-touch-icon.png\">
        <link rel=\"mask-icon\" href=\"";
            // line 47
            echo twig_escape_filter($this->env, ($context["cpAssetUrl"] ?? null), "html", null, true);
            echo "/images/icons/safari-pinned-tab.svg\" color=\"#e5422b\">
    ";
        }
        // line 49
        echo "
    <script type=\"text/javascript\">
        // Fix for Firefox autofocus CSS bug
        // See: http://stackoverflow.com/questions/18943276/html-5-autofocus-messes-up-css-loading/18945951#18945951
    </script>
    ";
        craft\helpers\Template::endProfile("block", "head");
    }

    // line 58
    public function block_body($context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("block", "body");
        craft\helpers\Template::endProfile("block", "body");
    }

    // line 59
    public function block_foot($context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("block", "foot");
        craft\helpers\Template::endProfile("block", "foot");
    }

    public function getTemplateName()
    {
        return "_layouts/base";
    }

    public function isTraitable()
    {
        return false;
    }

    public function getDebugInfo()
    {
        return array (  194 => 59,  186 => 58,  176 => 49,  171 => 47,  167 => 46,  163 => 45,  158 => 44,  155 => 43,  149 => 42,  146 => 41,  143 => 40,  141 => 39,  136 => 38,  131 => 37,  129 => 36,  122 => 32,  118 => 31,  114 => 29,  109 => 28,  99 => 60,  96 => 59,  94 => 58,  90 => 57,  86 => 56,  83 => 55,  81 => 28,  76 => 26,  73 => 25,  70 => 23,  68 => 21,  66 => 20,  64 => 18,  63 => 17,  62 => 16,  61 => 15,  59 => 12,  58 => 11,  57 => 10,  56 => 9,  55 => 8,  54 => 7,  51 => 6,  49 => 5,  47 => 4,  45 => 3,  43 => 2,  41 => 1,);
    }

    public function getSourceContext()
    {
        return new Source("", "_layouts/base", "/var/www/html/vendor/craftcms/cms/src/templates/_layouts/base.twig");
    }
}
