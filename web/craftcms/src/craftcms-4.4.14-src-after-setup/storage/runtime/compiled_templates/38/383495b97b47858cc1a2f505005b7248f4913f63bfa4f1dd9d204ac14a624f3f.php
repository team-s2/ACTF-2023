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

/*  */
class __TwigTemplate_b687b01786d731da0cf478ada6d58e031c7346bbb577f204fe9aa313260caa0c extends Template
{
    private $source;
    private $macros = [];

    public function __construct(Environment $env)
    {
        parent::__construct($env);

        $this->source = $this->getSourceContext();

        $this->parent = false;

        $this->blocks = [
        ];
    }

    protected function doDisplay(array $context, array $blocks = [])
    {
        $macros = $this->macros;
        craft\helpers\Template::beginProfile("template", "");
        craft\helpers\Template::preloadSingles(['view']);
        // line 1
        echo "<!DOCTYPE html>
<html lang=\"en-US\">
<head>
    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />
    <meta charset=\"utf-8\" />
    <title>Welcome to Craft CMS</title>
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover\" />
    <meta name=\"referrer\" content=\"origin-when-cross-origin\" />
    <style>
        html,
        body {
            font-size: 16px;
            -webkit-text-size-adjust: 100%;
            height: 100%;
            font-family: system-ui, BlinkMacSystemFont, -apple-system, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
        }

        body {
            margin: 0;
            padding: 0;
            background-color: hsl(212, 60%, 97%);
            color: hsl(209, 18%, 30%);
            display: flex;
        }

        h1 {
            margin-top: 0;
        }

        h2 {
            margin-top: 24px;
            font-size: 1em;
        }

        h2:first-child {
            margin-top: 0;
        }

        p {
            line-height: 1.4em;
            margin-bottom: 1.4em;
        }

        ul {
            line-height: 1.3em;
            padding-left: 20px;
            margin-bottom: 0;
        }

        ul li {
            margin-bottom: 0.35em;
        }

        a {
            color: #0B69A3;
            text-decoration: none;
        }

        a:hover {
            text-decoration: underline;
        }

        .go {
            color: #0B69A3;
        }

        .go:after {
            padding-left: 4px;
            content: '→';
            text-decoration: none !important;
        }

        small {
            color: hsl(211, 11%, 59%);
        }

        code {
            display: inline-block;
            color: #EF4E4E;
            padding: 0 2px;
            background: hsl(212, 60%, 97%);
            border-radius: 3px;
            line-height: 1.3;
            font-family: \"SFMono-Regular\", Consolas, \"Liberation Mono\", Menlo, Courier, monospace;
            font-size: 0.9em;
        }

        #container {
            flex-grow: 1;
        }

        #modal {
            background: #fff;
        }

        #aside {
            background: hsl(212, 60%, 97%);
        }

        .content {
            padding: 35px;
            padding-left: calc(35px + env(safe-area-inset-left));
            padding-right: calc(35px + env(safe-area-inset-right));
        }

        @media (min-width:768px) {
            #modal {
                display: flex;
            }

            #main {
                width: 50%;
                overflow: auto;
            }

            #aside {
                width: 50%;
                overflow: auto;
            }
        }

        @media (min-width:768px) and (min-height: 376px) {
            body {
                background-color: hsl(212, 50%, 93%);
                background-image: url(\"";
        // line 125
        echo twig_escape_filter($this->env, craft\helpers\Template::attribute($this->env, $this->source, craft\helpers\Template::attribute($this->env, $this->source, (isset($context["view"]) || array_key_exists("view", $context) ? $context["view"] : (craft\helpers\Template::fallbackExists("view") ? craft\helpers\Template::fallback("view") : null)), "getAssetManager", [], "method"), "getPublishedUrl", [0 => "@app/web/assets/installer/dist", 1 => true, 2 => "images/installer-bg.png"], "method"), "html", null, true);
        echo "\");
                background-repeat: no-repeat;
                background-size: cover;
                background-position: center center;
            }

            #container {
                display: flex;
                padding: 24px;
                align-items: center;
                justify-content: center;
            }

            #modal {
                height: 100%;
                max-width: 800px;
                max-height: 525px;
                border-radius: 4px;
                overflow: auto;
                box-shadow: 0 25px 100px rgba(0, 0, 0, 0.5);
            }

            #aside {
                overflow: auto;
            }
        }

    </style>
";
        // line 153
        $this->env->getFunction('head')->getCallable()();
        echo "</head>
<body class=\"ltr\">";
        // line 154
        $this->env->getFunction('beginBody')->getCallable()();
        echo "
<div id=\"container\">
    <div id=\"modal\">
        <div id=\"main\">
            <div class=\"content\">
                <h1>Welcome</h1>
                <p>Thanks for installing Craft CMS!</p>
                <p>You’re looking at the <code>index.twig</code> template file located in your
                    <code>templates/</code> folder. Once you’re ready to start building out your site’s
                    front end, you can replace this with something custom.</p>
                <p>If you’re new to Craft CMS, take some time to check out the resources on the right
                    when you get a chance&mdash;especially
                    <a href=\"https://craftcms.com/discord\" target=\"_blank\">Discord</a>
                    and <a href=\"http://craftcms.stackexchange.com/\" target=\"_blank\">Stack Exchange</a>.
                    The Craft community is full of smart, friendly, and helpful people!</p>
                <p><span class=\"go\"><a href=\"";
        // line 169
        echo twig_escape_filter($this->env, craft\helpers\UrlHelper::cpUrl(""), "html", null, true);
        echo "\">Go to your control panel</a></span></p>
            </div>
        </div>
        <div id=\"aside\">
            <div class=\"content\">
                <h2>Popular Resources</h2>
                <ul>
                    <li><a href=\"https://craftcms.com/docs/getting-started-tutorial/\" target=\"_blank\">Tutorial</a><br><small>Learn the basics.</small></li>
                    <li><a href=\"https://craftcms.com/docs/4.x/\" target=\"_blank\">Documentation</a><br><small>Read the official docs.</small></li>
                    <li><a href=\"https://craftcms.com/guides\" target=\"_blank\">Knowledge Base</a><br><small>Find answers to common problems.</small></li>
                    <li><a href=\"https://twitter.com/hashtag/craftcms\" target=\"_blank\">#craftcms</a><br><small>See the latest tweets about Craft.</small></li>
                    <li><a href=\"https://craftcms.com/discord\" target=\"_blank\">Discord</a><br><small>Meet the community.</small></li>
                    <li><a href=\"http://craftcms.stackexchange.com/\" target=\"_blank\">Stack Exchange</a><br><small>Get help and help others.</small></li>
                    <li><a href=\"https://craftquest.io/\" target=\"_blank\">CraftQuest</a><br><small>Watch unlimited video lessons and courses.</small></li>
                    <li><a href=\"http://craftlinklist.com/\" target=\"_blank\">Craft Link List</a><br><small>Stay in-the-know.</small></li>
                    <li><a href=\"https://nystudio107.com/blog\" target=\"_blank\">nystudio107 Blog</a><br><small>Learn Craft and modern web development.</small></li>
                </ul>
            </div>
        </div>
    </div>
</div>
";
        // line 190
        $this->env->getFunction('endBody')->getCallable()();
        echo "</body>
</html>
";
        craft\helpers\Template::endProfile("template", "");
    }

    public function getTemplateName()
    {
        return "";
    }

    public function getDebugInfo()
    {
        return array (  242 => 190,  218 => 169,  200 => 154,  196 => 153,  165 => 125,  39 => 1,);
    }

    public function getSourceContext()
    {
        return new Source("", "", "/var/www/html/templates/index.twig");
    }
}
