# craftcms

CVE-2023-41892

预期解：

使用CVE-2023-41892实现任意构造函数执行，然后利用imagick的msl写入webshell。

非预期：

使用pearcmd.php从文件包含到rce。
