<?php

$vendorDir = dirname(__DIR__);

return array (
  'yiisoft/yii2-symfonymailer' => 
  array (
    'name' => 'yiisoft/yii2-symfonymailer',
    'version' => '2.0.4.0',
    'alias' => 
    array (
      '@yii/symfonymailer' => $vendorDir . '/yiisoft/yii2-symfonymailer/src',
    ),
  ),
  'yiisoft/yii2-queue' => 
  array (
    'name' => 'yiisoft/yii2-queue',
    'version' => '2.3.5.0',
    'alias' => 
    array (
      '@yii/queue' => $vendorDir . '/yiisoft/yii2-queue/src',
      '@yii/queue/db' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/db',
      '@yii/queue/sqs' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/sqs',
      '@yii/queue/amqp' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/amqp',
      '@yii/queue/file' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/file',
      '@yii/queue/sync' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/sync',
      '@yii/queue/redis' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/redis',
      '@yii/queue/stomp' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/stomp',
      '@yii/queue/gearman' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/gearman',
      '@yii/queue/beanstalk' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/beanstalk',
      '@yii/queue/amqp_interop' => $vendorDir . '/yiisoft/yii2-queue/src/drivers/amqp_interop',
    ),
  ),
  'yiisoft/yii2-debug' => 
  array (
    'name' => 'yiisoft/yii2-debug',
    'version' => '2.1.22.0',
    'alias' => 
    array (
      '@yii/debug' => $vendorDir . '/yiisoft/yii2-debug/src',
    ),
  ),
  'samdark/yii2-psr-log-target' => 
  array (
    'name' => 'samdark/yii2-psr-log-target',
    'version' => '1.1.3.0',
    'alias' => 
    array (
      '@samdark/log' => $vendorDir . '/samdark/yii2-psr-log-target/src',
      '@samdark/log/tests' => $vendorDir . '/samdark/yii2-psr-log-target/tests',
    ),
  ),
  'creocoder/yii2-nested-sets' => 
  array (
    'name' => 'creocoder/yii2-nested-sets',
    'version' => '0.9.0.0',
    'alias' => 
    array (
      '@creocoder/nestedsets' => $vendorDir . '/creocoder/yii2-nested-sets/src',
    ),
  ),
  'craftcms/generator' => 
  array (
    'name' => 'craftcms/generator',
    'version' => '1.6.1.0',
    'alias' => 
    array (
      '@craft/generator' => $vendorDir . '/craftcms/generator/src',
    ),
    'bootstrap' => 'craft\\generator\\Extension',
  ),
  'yiisoft/yii2-shell' => 
  array (
    'name' => 'yiisoft/yii2-shell',
    'version' => '2.0.5.0',
    'alias' => 
    array (
      '@yii/shell' => $vendorDir . '/yiisoft/yii2-shell',
    ),
    'bootstrap' => 'yii\\shell\\Bootstrap',
  ),
);
