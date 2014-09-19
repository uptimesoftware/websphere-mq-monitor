package com.uptimesoftware.uptime.plugin;

import java.util.*;
import java.io.*;
import com.ibm.mq.*;
import com.ibm.mq.pcf.*;

import ro.fortsoft.pf4j.PluginWrapper;
import com.uptimesoftware.uptime.plugin.api.Extension;
import com.uptimesoftware.uptime.plugin.api.Plugin;
import com.uptimesoftware.uptime.plugin.api.PluginMonitor;
import com.uptimesoftware.uptime.plugin.monitor.PluginMonitorVariable;
import com.uptimesoftware.uptime.plugin.monitor.MonitorState;
import com.uptimesoftware.uptime.plugin.monitor.Parameters;

/**
 * 
 * @author chris
 */
public class MonitorWebsphereMQ extends Plugin {

	public MonitorWebsphereMQ(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Extension
	public static class UptimeMonitorWebsphereMQ extends PluginMonitor {

		private Integer port = 0;
		private String channelName = "";
		private String message = "";
		private String hostname = "";
		private Boolean showSystemQueues = false;
		private String queueFilter = "";
		private String queueExists = "";
		private List<String> queueFilterList = new ArrayList<String>();
		private List<String> queueExistsList = new ArrayList<String>();

		/*
		 * Set parameters reads the input values from the XML that defines the monitor
		 * The parameters object has all the methods for fetching the values.
		 */
		@Override
		public void setParameters(Parameters params) {

			port = params.getInteger("port");
			channelName = params.getString("channelName");
			showSystemQueues = params.getBoolean("showSystemQueues");
			queueFilter = params.getString("queueFilter");
			queueExists = params.getString("queueExists");
			hostname = params.getString("hostname");

			if (queueFilter != null) {
				String[] queues = queueFilter.split(",");
				queueFilterList.addAll(Arrays.asList(queues));
			}

			if (queueExists != null) {
				String[] queues = queueExists.split(",");
				queueExistsList.addAll(Arrays.asList(queues));
			}
		}

		private PluginMonitorVariable getMonitorVariable(String queueName, String objectName, Integer value) {
			PluginMonitorVariable monitorVariable = new PluginMonitorVariable(queueName, value.toString());
			monitorVariable.setObjectName(objectName);
			return monitorVariable;
		}

		/*
		 * monitor method is called when the monitor runs and this is where your logic goes
		 * to perform your monitoring task and to set the output variables, monitor message
		 * and status.
		 */
		@Override
		public void monitor() {
			// CustomOutputParser parser = new CustomOutputParser();
			PCFMessageAgent pcfMessageAgent = null;

			// uptime 6
			MonitorState worstState = MonitorState.OK;

			try {
				pcfMessageAgent = new PCFMessageAgent(hostname, port, channelName);
				PCFMessage pcfMessage = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q);

				pcfMessage.addParameter(CMQC.MQCA_Q_NAME, "*");
				pcfMessage.addParameter(CMQC.MQIA_Q_TYPE, CMQC.MQQT_LOCAL);

				PCFMessage[] pcfMessageArray = pcfMessageAgent.send(pcfMessage);

				for (int i = 0; i < pcfMessageArray.length; i++) {
					PCFMessage pcfMessageResponse = pcfMessageArray[i];

					String queueName = (String) pcfMessageResponse.getParameterValue(CMQC.MQCA_Q_NAME);

					queueName = queueName.replace(".", "_").replace(":", "").trim();

					Integer currentQueueDepth = (Integer) pcfMessageResponse
							.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH);
					Integer maxQueueDepth = (Integer) pcfMessageResponse.getParameterValue(CMQC.MQIA_MAX_Q_DEPTH);

					// Not used Strings, but leaving them commented out for now.
					// String currentQueueDepthValue = queueName + "." + "currentQueueDepth " + currentQueueDepth;
					// String maxQueueDepthValue = queueName + "." + "maxQueueDepth " + maxQueueDepth;

					queueExistsList.remove(queueName);

					if (queueFilterList.isEmpty()) {
						if (queueName.startsWith("SYSTEM")) {
							if (showSystemQueues) {
								// addVariable(parser.parseLine(currentQueueDepthValue));
								addVariable(getMonitorVariable(queueName, "currentQueueDepth", currentQueueDepth));
								addVariable(getMonitorVariable(queueName, "maxQueueDepth", maxQueueDepth));
								// addVariable(parser.parseLine(maxQueueDepthValue));
							}
						}

						if (!queueName.startsWith("SYSTEM")) {
							// addVariable(parser.parseLine(currentQueueDepthValue));
							// addVariable(parser.parseLine(maxQueueDepthValue));
							addVariable(getMonitorVariable(queueName, "currentQueueDepth", currentQueueDepth));
							addVariable(getMonitorVariable(queueName, "maxQueueDepth", maxQueueDepth));
						}

					} else if (queueFilterList.contains(queueName)) {
						if (queueName.startsWith("SYSTEM")) {
							if (showSystemQueues) {
								// addVariable(parser.parseLine(currentQueueDepthValue));
								// addVariable(parser.parseLine(maxQueueDepthValue));
								addVariable(getMonitorVariable(queueName, "currentQueueDepth", currentQueueDepth));
								addVariable(getMonitorVariable(queueName, "maxQueueDepth", maxQueueDepth));
							}
						}

						if (!queueName.startsWith("SYSTEM")) {
							// addVariable(parser.parseLine(currentQueueDepthValue));
							// addVariable(parser.parseLine(maxQueueDepthValue));
							addVariable(getMonitorVariable(queueName, "currentQueueDepth", currentQueueDepth));
							addVariable(getMonitorVariable(queueName, "maxQueueDepth", maxQueueDepth));
						}
					}
				}

				if (!queueExistsList.isEmpty()) {
					message += "The following required queues were not found\r";
					Iterator<String> queueIterator = queueExistsList.iterator();
					while (queueIterator.hasNext()) {
						message += "Queue: " + queueIterator.next() + "\r";
						worstState = MonitorState.CRIT;
					}
				}

				message += "Monitor ran successfully";
				setState(worstState);

			} catch (MQException mqe) {
				message = "MQ Exception in monitor. " + mqe.getMessage()
						+ PCFConstants.lookupReasonCode(mqe.reasonCode);
				setState(MonitorState.CRIT);
			} catch (IOException ioe) {
				message = "IO Exception in monitor. " + ioe.getMessage();
				setState(MonitorState.CRIT);
			} catch (Exception e) {
				message = "General Exception in monitor. " + e.getMessage();
				setState(MonitorState.CRIT);
			} finally {
				try {
					pcfMessageAgent.disconnect();
				} catch (Exception e) {
					// Nothing;
				}
			}
			setMessage(message);
		}
	}

}
