[![Build Status](https://travis-ci.org/voku/urlify.png?branch=master)](https://travis-ci.org/voku/urlify)
[![Coverage Status](https://coveralls.io/repos/github/voku/urlify/badge.svg?branch=master)](https://coveralls.io/github/voku/urlify?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9904d596b8514891a38cb3b569cd4d95)](https://www.codacy.com/app/voku/urlify)
[![Latest Stable Version](https://poser.pugx.org/voku/urlify/v/stable)](https://packagist.org/packages/voku/urlify) 
[![Total Downloads](https://poser.pugx.org/voku/urlify/downloads)](https://packagist.org/packages/voku/urlify) 
[![License](https://poser.pugx.org/voku/urlify/license)](https://packagist.org/packages/voku/urlify)
[![Donate to this project using Paypal](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.me/moelleken)
[![Donate to this project using Patreon](https://img.shields.io/badge/patreon-donate-yellow.svg)](https://www.patreon.com/voku)

# 🔗 URLify

## Description

Convert any string into an valid and readable string for usage in the url.

This is a PHP port of "URLify.js" from the Django project + fallback via "Portable ASCII".
We handles symbols from many languages via an matching-array and others via "ASCII::to_transliterate()".

- https://github.com/django/django/blob/master/django/contrib/admin/static/admin/js/urlify.js
- https://github.com/voku/portable-ascii
- https://github.com/voku/portable-utf8

## Install via "composer require"

```shell
composer require voku/urlify
```

## Usage:

namespace: "voku\helper\URLify"

#### To generate slugs for URLs:

```php
echo URLify::filter(' J\'étudie le français ');
// "J-etudie-le-francais"

echo URLify::filter('Lo siento, no hablo español.');
// "Lo-siento-no-hablo-espanol"
```

#### To generate slugs for file names:

```php
echo URLify::filter('фото.jpg', 60, '', true);
// "foto.jpg"
```

#### To simply transliterate characters:

```php
echo URLify::downcode('J\'étudie le français');
// "J'etudie le francais"

echo URLify::downcode('Lo siento, no hablo español.');
// "Lo siento, no hablo espanol."

/* Or use transliterate() alias: */

echo URLify::transliterate('Lo siento, no hablo español.');
// "Lo siento, no hablo espanol."
```

#### To extend the character list:

```php
URLify::add_chars(array(
  '¿' => '?', '®' => '(r)', '¼' => '1/4',
  '½' => '1/2', '¾' => '3/4', '¶' => 'P'
));

echo URLify::downcode('¿ ® ½ ¼ ¾ ¶');
// "? (r) 1/2 1/4 3/4 P"
```

#### To extend or replace the default replacing list:

```php
URLify::add_array_to_seperator(array(
  "/®/"
));

echo URLify::filter('¿ ® ½ ¼ ¾ ¶');
// "12-14-34-P"
```

#### To extend the list of words to remove for one language:

```php
URLify::remove_words(array('remove', 'these', 'too'), 'de');
```

#### To prioritize a certain language map:

```php
echo URLify::filter(' Ägypten und Österreich besitzen wie üblich ein Übermaß an ähnlich öligen Attachés ', 60, 'de');
// "Aegypten-und-Oesterreich-besitzen-wie-ueblich-ein-Uebermass-aehnlich-oeligen-Attaches"
   
echo URLify::filter('Cağaloğlu, çalıştığı, müjde, lazım, mahkûm', 60, 'tr');
// "Cagaloglu-calistigi-mujde-lazim-mahkum"
```
Please note that the "ü" is transliterated to "ue" in the first case, whereas it results in a simple "u" in the latter.

## Available languages

- Arabic: 'ar'
- Austrian (German): 'de_at' 
- Austrian (French): 'fr_at'
- Azerbaijani: 'az'
- Bulgarian: 'bg'
- Burmese: 'by'
- Croatian: 'hr'
- Czech: 'cs'
- Danish: 'da'
- English: 'en'
- Esperanto: 'eo'
- Estonian: 'et'
- Finnish: 'fi'
- French: 'fr'
- Georgian: 'ka'
- German: 'de'
- Greek: 'el' 
- Hindi: 'hi'
- Hungarian: 'hu'
- Kazakh: 'kk'
- Latvian: 'lv'
- Lithuanian: 'lt'
- Norwegian: 'no'
- Polish: 'pl'
- Romanian: 'ro'
- Russian: 'ru'
- Serbian: 'sr'
- Slovak: 'sk'
- Swedish: 'sv'
- Switzerland (German): 'de_ch' 
- Switzerland (French): 'fr_ch' 
- Turkish: 'tr'
- Ukrainian: 'uk'
- Vietnamese: 'vn'

## Support

For support and donations please visit [Github](https://github.com/voku/urlify/) | [Issues](https://github.com/voku/urlify/issues) | [PayPal](https://paypal.me/moelleken) | [Patreon](https://www.patreon.com/voku).

For status updates and release announcements please visit [Releases](https://github.com/voku/urlify/releases) | [Twitter](https://twitter.com/suckup_de) | [Patreon](https://www.patreon.com/voku/posts).

For professional support please contact [me](https://about.me/voku).

## Thanks

- Thanks to [GitHub](https://github.com) (Microsoft) for hosting the code and a good infrastructure including Issues-Managment, etc.
- Thanks to [IntelliJ](https://www.jetbrains.com) as they make the best IDEs for PHP and they gave me an open source license for PhpStorm!
- Thanks to [Travis CI](https://travis-ci.com/) for being the most awesome, easiest continous integration tool out there!
- Thanks to [StyleCI](https://styleci.io/) for the simple but powerfull code style check.
- Thanks to [PHPStan](https://github.com/phpstan/phpstan) && [Psalm](https://github.com/vimeo/psalm) for relly great Static analysis tools and for discover bugs in the code!
