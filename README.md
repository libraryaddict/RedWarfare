# RedWarfare
The large majority of Red Warfare's code

To get this running you will need a MySQL server and a redis server.
You will need to change the settings in MysqlManager and RedisManager, its not user friendly as it was hardcoded.
It was hardcoded namely because I didn't believe it would ever be released to the public,
and if the plugin was leaked then I'll need to change the login details regardless.

For the build server, it should require some work to get running.

You may notice that some references to outside projects are missing, namely a server spinner and manager. Those managed the servers themselves.

In the process of updating to 1.12.2 some features may have been broken.


The maven structure is a bit weird and doesn't follow convention, when I created the project the folders were not created properly and I didn't want to waste time debugging a minor issue.


Another point of interest is the exception handling. Namely: UtilError.handle(e);

I wasn't sure how to log all exceptions and was trying this, by the time I realized it was a bad method it was not worth changing back.
