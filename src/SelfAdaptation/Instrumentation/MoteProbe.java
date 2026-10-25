package SelfAdaptation.Instrumentation;

import IotDomain.Gateway;
import IotDomain.LoraTransmission;
import IotDomain.Mote;
import IotDomain.NetworkEntity;
import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;

/**
 * A class representing methods for probing.
 */
public class MoteProbe {
    /**
     * A list with feedBackLoops using the probe.
     */
    private GenericFeedbackLoop genericFeedbackLoop;

    /**
     * Constructs a MoteProbe with no FeedBackLoops using it.
     */
    public MoteProbe(){

    }

    /**
     * Returns a list of GenericFeedbackLoops using the probe.
     * @return  A list of GenericFeedbackLoops using the probe.
     */
    public GenericFeedbackLoop getGenericFeedbackLoop() {
        return genericFeedbackLoop;
    }

    /**
     * Sets a GenericFeedbackLoop using the probe.
     * @param genericFeedbackLoop The FeedbackLoop to set.
     */
    public void setGenericFeedbackLoop(GenericFeedbackLoop genericFeedbackLoop){
        this.genericFeedbackLoop =genericFeedbackLoop;
    }

    /**
     * Returns the highest signal with which any of the gateways received the latest packet of a given mote.
     * @param mote The mote to generate the graph of.
     * @return The highest received signal
     * Returns {@code null} if no gateway received the latest packet without collision (or nothing was sent yet).
     */
    public Double getHighestReceivedSignal(Mote mote) {
        int run = mote.getEnvironment().getNumberOfRuns() - 1;
        Double highestReceivedSignal = null;
        for (LoraTransmission transmission : mote.getLatestTransmissions()) {
            if (Mote.wasReceived(transmission, run)
                    && (highestReceivedSignal == null || transmission.getTransmissionPower() > highestReceivedSignal)) {
                highestReceivedSignal = transmission.getTransmissionPower();
            }
        }
        return highestReceivedSignal;
    }

    /**
     * Returns the spreading factor of a given mote.
     * @param mote The mote to generate the graph of.
     * @return the spreading factor of the mote
     */
    public Integer getSpreadingFactor(NetworkEntity mote) {
        return mote.getSF();
    }

    /**
     * Returns The distance to the nearest Gateway.
     * @param mote The given mote to find the shortest distance.
     * @return The distance to the nearest Gateway from the mote's current position.
     * Returns {@code null} if there are no gateways.
     */
    public Double getShortestDistanceToGateway(Mote mote) {
        Double shortestDistance = null;
        for (Gateway gateway : mote.getEnvironment().getGateways()) {
            double distance = Math.hypot(gateway.getXPos() - mote.getXPos(), gateway.getYPos() - mote.getYPos());
            if (shortestDistance == null || distance < shortestDistance) {
                shortestDistance = distance;
            }
        }
        return shortestDistance;
    }

    /**
     * Returns the power setting of a specific mote.
     * @param mote The mote to get the power setting of.
     * @return The power setting of the mote.
     */
    public Integer getPowerSetting(NetworkEntity mote) {

        return mote.getTransmissionPower();

    }

    /**
     * Triggers the feedback loop.
     * @param gateway
     * @param devEUI
     */
    public void trigger(Gateway gateway, Long devEUI){
        Boolean found = false;
        Mote sender = null;
        for (Mote mote :gateway.getEnvironment().getMotes()){
            if(mote.getEUI() == devEUI){
                sender = mote;
                found = true;
            }
        }
        if(found) {
            if(getGenericFeedbackLoop().isActive()) {
                getGenericFeedbackLoop().adapt(sender, gateway);
            }
        }
    }

}
