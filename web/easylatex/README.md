# Easylatex

Expect Solution:

1. Forge JWT token to be VIP
2. Post a note with a theme with js on www.jsdelivr.com (controlable)
3. use iframe.contentWindow to bypass CSP (only `note` route has CSP)
4. fetch `/vip` with malformed `username` param to send request with cookie to your favorite webhook

Other Solution:

1. puppeteer will resolve `../` to upper directory, so share with `share/../preview?tex=xxx` to bypass CSP