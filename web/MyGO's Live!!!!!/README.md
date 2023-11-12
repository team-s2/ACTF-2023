# MyGO's Live

## Description
...  

## Info
Inspired by SekaiCTF2023#Scanner Service. This modified problem disallows direct echoing of standard output.  
However, an argument `-oN` gives your the ability to redirect error messages to standard output.  
You can match the file name by wildcarding.  
One possible payload: `/checker?url=1:8080%09-iL%09/flag-????????????????-oN%09/dev/stdout`

---

Unexpected solution:   
Since it runs on a static container, you will directly get the flag in response if someone else got it since the last restart.