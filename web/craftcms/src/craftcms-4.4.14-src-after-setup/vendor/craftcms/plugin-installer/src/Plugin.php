<?php
/**
 * @link    https://craftcms.com/
 * @license MIT
 */

namespace craft\composer;

use Composer\Composer;
use Composer\IO\IOInterface;
use Composer\Plugin\PluginInterface;

/**
 * Plugin is the Composer plugin that registers the Craft CMS composer installer.
 *
 * @author Pixel & Tonic, Inc. <support@craftcms.com>
 */
class Plugin implements PluginInterface
{
    /**
     * @var Installer
     */
    private $installer;

    /**
     * @inheritdoc
     */
    public function activate(Composer $composer, IOInterface $io)
    {
        // Register the plugin installer
        $this->installer = new Installer($io, $composer, 'craft-plugin');
        $composer->getInstallationManager()->addInstaller($this->installer);

        // Is this a plugin at root? Elementary, my dear Watson 🕵️!
        if ($this->installer->supports($composer->getPackage()->getType())) {
            $this->installer->addPlugin($composer->getPackage(), true);
        }
    }

    /**
     * @inheritdoc
     */
    public function deactivate(Composer $composer, IOInterface $io)
    {
        $composer->getInstallationManager()->removeInstaller($this->installer);
    }

    /**
     * @inheritdoc
     */
    public function uninstall(Composer $composer, IOInterface $io)
    {
    }
}
