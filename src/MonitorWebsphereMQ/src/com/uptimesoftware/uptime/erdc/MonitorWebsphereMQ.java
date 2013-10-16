package com.uptimesoftware.uptime.erdc;

import com.uptimesoftware.uptime.erdc.baseclass.MonitorWithMonitorVariables;
import com.uptimesoftware.uptime.erdc.custom.CustomOutputParser;
/*
import com.uptimesoftware.uptime.erdc.custom.MonitorVariable;
import com.uptimesoftware.uptime.erdc.helper.ErdcRunResult;
import com.uptimesoftware.uptime.ranged.RangedObject;
import com.uptimesoftware.uptime.ranged.RangedObjectValue;
*/
import com.uptimesoftware.uptime.base.util.Parameters;
import java.util.*;
import java.io.*;
import com.ibm.mq.*;
import com.ibm.mq.pcf.*;

/**
 *
 * @author chris
 */
public class MonitorWebsphereMQ extends MonitorWithMonitorVariables {

    private Integer port = 0;
    private String channelName = "";
    private String message = "";
    private Boolean showSystemQueues = false;
    private String queueFilter = "";
    private String queueExists = "";
    private List<String> queueFilterList = new ArrayList<String>();
    private List<String> queueExistsList = new ArrayList<String>();

    /**
     * Creates a new instance of MonitorWebsphereMQ
     */
    public MonitorWebsphereMQ() {
    }

    /*
     * Set parameters reads the input values from the XML that defines the monitor
     * The parameters object has all the methods for fetching the values.
     */
    @Override
    public void setParameters(Parameters params, Long instanceId) {
        super.setParameters(params, instanceId);

        port = parameters.getIntegerParameter("port");
        channelName = parameters.getStringParameter("channelName");
        showSystemQueues = parameters.getBooleanParameter("showSystemQueues");
        queueFilter = parameters.getStringParameter("queueFilter");
        queueExists = parameters.getStringParameter("queueExists");

        if (queueFilter != null) {
            String[] queues = queueFilter.split(",");
            queueFilterList.addAll(Arrays.asList(queues));
        }

        if (queueExists != null) {
            String[] queues = queueExists.split(",");
            queueExistsList.addAll(Arrays.asList(queues));
        }
    }

    /*
     * monitor method is called when the monitor runs and this is where your logic goes
     * to perform your monitoring task and to set the output variables, monitor message
     * and status.
     */
    @Override
    protected void monitor() {
        CustomOutputParser parser = new CustomOutputParser();
        PCFMessageAgent pcfMessageAgent = null;
        
        // uptime 6
        ErdcTransientState worstState = ErdcTransientState.OK;
        
        try {


            pcfMessageAgent = new PCFMessageAgent(parameters.getHostname(), port, channelName);
            PCFMessage pcfMessage = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q);

            pcfMessage.addParameter(CMQC.MQCA_Q_NAME, "*");
            pcfMessage.addParameter(CMQC.MQIA_Q_TYPE, MQC.MQQT_LOCAL);

            PCFMessage[] pcfMessageArray = pcfMessageAgent.send(pcfMessage);


            for (int i = 0; i < pcfMessageArray.length; i++) {
                PCFMessage pcfMessageResponse = pcfMessageArray[i];

                String queueName = (String) pcfMessageResponse.getParameterValue(CMQC.MQCA_Q_NAME);



                queueName = queueName.replace(".", "_").replace(":", "").trim();

                Integer currentQueueDepth = (Integer) pcfMessageResponse.getParameterValue(CMQC.MQIA_CURRENT_Q_DEPTH);
                Integer maxQueueDepth = (Integer) pcfMessageResponse.getParameterValue(CMQC.MQIA_MAX_Q_DEPTH);
                String currentQueueDepthValue = queueName + "." + "currentQueueDepth " + currentQueueDepth;
                String maxQueueDepthValue = queueName + "." + "maxQueueDepth " + maxQueueDepth;

                queueExistsList.remove(queueName);

                if (queueFilterList.isEmpty()) {

                    if (queueName.startsWith("SYSTEM")) {
                        if (showSystemQueues) {
                            addVariable(parser.parseLine(currentQueueDepthValue));
                            addVariable(parser.parseLine(maxQueueDepthValue));
                        }

                    }

                    if (!queueName.startsWith("SYSTEM")) {
                        addVariable(parser.parseLine(currentQueueDepthValue));
                        addVariable(parser.parseLine(maxQueueDepthValue));
                    }

                } else if (queueFilterList.contains(queueName)) {
                    if (queueName.startsWith("SYSTEM")) {
                        if (showSystemQueues) {
                            addVariable(parser.parseLine(currentQueueDepthValue));
                            addVariable(parser.parseLine(maxQueueDepthValue));
                        }
                    }

                    if (!queueName.startsWith("SYSTEM")) {
                        addVariable(parser.parseLine(currentQueueDepthValue));
                        addVariable(parser.parseLine(maxQueueDepthValue));
                    }
                }

            }

            if (!queueExistsList.isEmpty()) {
                message += "The following required queues were not found\r";
                Iterator queueIterator = queueExistsList.iterator();
                while (queueIterator.hasNext()) {
                    message += "Queue: " + queueIterator.next() + "\r";
                    worstState = ErdcTransientState.CRIT;
                }

            }

            message += "Monitor ran successfully";
            setState(worstState);


        } catch (MQException mqe) {

            message = "MQ Exception in monitor. " + mqe.getMessage() + PCFConstants.lookupReasonCode(mqe.reasonCode);
            
            setState(ErdcTransientState.CRIT);
        } catch (IOException ioe) {

            message = "IO Exception in monitor. " + ioe.getMessage();
            setState(ErdcTransientState.CRIT);
        } catch (Exception e) {

            message = "General Exception in monitor. " + e.getMessage();
            setState(ErdcTransientState.CRIT);
        } finally {
            try {
                pcfMessageAgent.disconnect();
            } catch (Exception e) {
                
            }
        }
        setMessage(message);
    }
/*
    @Override
    public ErdcRunResult getResults() {
        ErdcRunResult result = super.getResults();

        List<RangedObject> rangedObjects = new ArrayList<RangedObject>();

        List<MonitorVariable> variables = getVariables();
        for (MonitorVariable variable : variables) {
            variable.setSampleTime(result.getSampleTime());
            if (variable.isRanged()) {
                RangedObject rangedObject = convertVariableToRangedObject(variable);
                if (rangedObject != null) {
                    rangedObjects.add(rangedObject);
                }
            }
        }

        if (!rangedObjects.isEmpty()) {
            result.setRangedObjects(rangedObjects);
        }

        return result;
    }

    public RangedObject convertVariableToRangedObject(MonitorVariable variable) {
        RangedObject object = new RangedObject();

        String objectType = variable.getObjectType();
        if (objectType != null) {
            object.setObjectType(objectType);
        }
        object.setInstanceId(getInstanceId());
        object.setObjectName(variable.getObjectName());

        RangedObjectValue value = new RangedObjectValue();
        String variableName = variable.getName();
        String variableValue = variable.getValue();

        value.setName(variableName);

        Double doubleValue = null;
        try {
            doubleValue = new Double(variableValue);
        } catch (NumberFormatException e) {
            return null;
        }
        value.setValue(doubleValue);

        value.setSampleTime(variable.getSampleTime());
        object.addValue(value);
        return object;
    }
*/
}
