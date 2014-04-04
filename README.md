# P2P-Chat

A chat application that uses peer-to-peer (Hybrid model).  
There are no plans yet for populating this, it was mainly done for practice and fun.

## How to

In order to productively use the application, you need to either:  
- setup a central server where peers are supposed to use to know about eachother.  
You can do that by running the HybridCentralPoint.java in the directory centralpoint.  
- If you do not want to have a central server, you could just use the application  
directly but you'd need to know peer ip/port in order to connect to them on your own.

To run the actual application, compile and execute the file UserInfo.java in directory  
p2pchat.  You should not run P2PChat.java on it's own, that's because UserInfo.java  
executes that class after retreiving user information.  It's quite confusing but I'll  
find a better way to do that in the near future.  Also P2PChat.java has no main method.

## Contributions

Either send me an e-mail (<f.fallen45@gmail.com>) and attach your patch or do a pull request on GitHub.