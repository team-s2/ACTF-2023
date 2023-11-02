# craftcms

CVE-2023-41892

预期解：

使用imagick的msl写入webshell。具体可参考：http://www.bmth666.cn/2023/09/26/CVE-2023-41892-CraftCMS%E8%BF%9C%E7%A8%8B%E4%BB%A3%E7%A0%81%E6%89%A7%E8%A1%8C%E6%BC%8F%E6%B4%9E%E5%88%86%E6%9E%90/。

非预期：

使用pearcmd.php从文件包含到rce。