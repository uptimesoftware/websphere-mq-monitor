# Websphere MQ Monitor

See http://uptimesoftware.github.io for more information.

### Tags 
 plugin   websphere   MQ  

### Category

plugin

### Version Compatibility


  
* Websphere MQ Monitor 2.0 - 7.2, 7.1, 7.0, 6.0
  

  
* Websphere MQ Monitor 1.1 - 6.0
  

  
* Websphere MQ Monitor 1.0 - 5.5, 5.4, 5.3
  


### Description
Monitor for Websphere MQ 7.x


### Supported Monitoring Stations

7.2, 7.1, 7.0, 6.0

### Supported Agents
None; no agent required

### Installation Notes
<p><a href="https://github.com/uptimesoftware/uptime-plugin-manager">Install using the up.time Plugin Manager</a></p>


### Dependencies
<p>n/a</p>


### Input Variables

* MQ Manager Port - The port the MQ manager is listening on

* MQ Channel Name - The MQ Connection Channel to communicate through

* Queue List - The MQ Queues to return (Comma separated list)

* Queue Check - The MQ Queues to check exist (Comma separated list)

* Show SYSTEM queues - Whether the monitor returns the SYSTEM queue information


### Output Variables


* Current Queue Depth - Number of messages currently in the queue

* Maximum Queue Depth - Maximum message depth of the queue


### Languages Used

* Java

