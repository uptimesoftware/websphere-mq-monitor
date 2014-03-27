----------------------------------------------------
WebSphere MQ Plugin Monitor - up.time software
----------------------------------------------------


Prerequisites:
----------------------------------------------------
All *.jar files from Websphere MQ system in the following directory:
-<Websphere_MQ_Dir>\java\lib\

Default directory is:
-C:\program files\IBM\WebSphere MQ\java\lib\


Installation
----------------------------------------------------

On the up.time Monitoring Station:
1. Create a directory named "websphere_mq" in "<uptime_dir>/core"

2. Place all the *.jar files from your WebSphere MQ server
   (<Websphere_MQ_Dir>\java\lib\) into the newly created directory:
   "<uptime_dir>/core/websphere_mq"

3. Place the attached jar file "MonitorWebsphereMQ.jar" into the directory:
   "<uptime_dir>/core/websphere_mq"

4. Place the XML file in the uptime base directory and run the following on the command line:
> cd <uptime_dir>
> scripts\erdcloader -x MonitorWebsphereMQ.xml

5. Add the following line to the following file: <uptime_dir>/wrapper.conf:

wrapper.java.classpath.2=%UPTIMEROOT%/core/websphere_mq/*.jar


6. Restart the "up.time Data Collector" (core) service

You should now have a new service monitor named "WebSphere MQ" in up.time!