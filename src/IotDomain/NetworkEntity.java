package IotDomain;




import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * An  abstract class representing an entity active in the LoraWan network
 */
public abstract class NetworkEntity implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * A boolean to know when the entity is transmitting
     */

    private Boolean isTransmitting;

    /**
     * A list representing the power setting of every transmission.
     */
    private LinkedList<List<Pair<Integer,Integer>>> powerSettingHistory;
    /**
     * A list representing the spreading factor of every transmission.
     */
    private LinkedList<List<Integer>> spreadingFactorHistory;
    /**
     * An unsinged long representing the 64 bit unique identifier.
     */
    private final Long EUI;
    /**
     * The x-coordinate of the entity
     */
    private Integer xPos = 0;
    /**
     * The y-coordinate of the entity.
     */
    private Integer yPos = 0;
    /**
     * A map of the environment the entity is placed in.
     */
    private Environment environment;
    /**
     * The transmission power of the entity.
     */
    private Integer transmissionPower;
    /**
     * The levels of power in between which it can discriminate.
     */
    private final Double transmissionPowerThreshold;
    /**
     *  A map with the transmissions received by the entity and if they collided with an other packet.
     */
    private LinkedList<LinkedHashMap<LoraTransmission,Boolean>> receivedTransmissions = new LinkedList<>();
    /**
     *  A list with the transmissions transmitted by the entity
     */
    private LinkedList<LinkedList<LoraTransmission>> sentTransmissions = new LinkedList<>();

    /**
     * Stores the per-transmission energy usage history for each run.
     */
    private LinkedList<LinkedList<Double>> usedEnergyHistory;

    /**
     * Stores the cumulative energy usage for each run.
     */
    private LinkedList<Double> totalEnergyConsumedPerRun;

    /**
     * If the mote is enabled in the current simulation.
     */
    private Boolean enabled;

    /**
     *  A constructor generating a Network with a given x-position, y-position, environment and transmission power.
     * @param xPos  The x-coordinate of the entity on the map.
     * @param yPos  The y-coordinate of the entity on the map.
     * @param environment   The map of the environment.
     * @param transmissionPower   The transmission power of the entity.
     * @param SF    The spreading factor of the entity.
     * @param transmissionPowerThreshold The threshold for discriminating different transmissions.
     * @Post    If the x-coordinate was valid, it is set.
     * @Post    If the x-coordinate was not valid, it is set to 0.
     * @Post    If the y-coordinate was valid, it is set.
     * @Post    If the y-coordinate was not valid, it is set to 0.
     * @Post    If the transmission power was valid, it is set.
     * @Post    If the transmission power was not valid, it is set to 0.
     *
     */
    
    public NetworkEntity(Long EUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower, Integer SF,
                         Double transmissionPowerThreshold){
        this.environment = environment;
        if(environment.isValidXpos(xPos)){
            this.xPos = xPos;
        }
        if(environment.isValidYpos(yPos)){
            this.yPos = yPos;
        }

        if(isValidTransmissionPower(transmissionPower)){
            this.transmissionPower = transmissionPower;
        }
        else
            this.transmissionPower = 0;
        if(isValidSF(SF)){
            this.SF = SF;
        }
        this.transmissionPowerThreshold = transmissionPowerThreshold;
        this.EUI = EUI;
        powerSettingHistory = new LinkedList<>();
        powerSettingHistory.add(new LinkedList<>());
        spreadingFactorHistory = new LinkedList<>();
        spreadingFactorHistory.add(new LinkedList<>());
        receivedTransmissions.add(new LinkedHashMap<>());
        sentTransmissions.add(new LinkedList<>());
        usedEnergyHistory = new LinkedList<>();
        usedEnergyHistory.add(new LinkedList<>());
        totalEnergyConsumedPerRun = new LinkedList<>();
        totalEnergyConsumedPerRun.add(0.0);
        isTransmitting =false;
        enabled = true;
    }

    /**
     * Returns the transmission power threshold.
     * @return The transmission power threshold.
     */
    public Double getTransmissionPowerThreshold() {
        return transmissionPowerThreshold;
    }

    /**
     *
     * @return The environment of the entity.
     */
    
    
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Return the power setting history of the entity.
     * @return The power setting history of the entity.
     */
    public List<Pair<Integer,Integer>> getPowerSettingHistory(Integer run) {
        return powerSettingHistory.get(run);
    }

    /**
     * Return the spreading factor history of the entity.
     * @return The spreading factor history of the entity.
     */
    public List<Integer> getSpreadingFactorHistory(Integer run) {
        return spreadingFactorHistory.get(run);
    }

    /**
     *  Returns The transmissions sent by the entity.
     * @return The transmissions sent by the entity.
     */
    
    
    public LinkedList<LoraTransmission> getSentTransmissions(Integer run) {
        return sentTransmissions.get(run);
    }

    /**
     * Returns all transmissions with collisions included
     * @return all transmissions with collisions included
     */
    public LinkedHashMap<LoraTransmission,Boolean> getAllReceivedTransmissions(Integer run){
        return receivedTransmissions.get(run);
    }

    /**
     * Returns only the transmission actually received by the gateway.
     * @return only the transmission actually received by the gateway.
     */
    public LinkedList<LoraTransmission> getReceivedTransmissions(Integer run){
        LinkedList<LoraTransmission> transmissions = new LinkedList<>();
        for(LoraTransmission transmission : getAllReceivedTransmissions(run).keySet()){
            if(!getAllReceivedTransmissions(run).get(transmission))
                transmissions.add(transmission);
        }
        return transmissions;
    }

    /**
     * Checks if a transmission power is valid.
     * @param transmissionPower The transmission power to check.
     * @return true if the transmission power is valid. False otherwise.
     */
    
    public static boolean isValidTransmissionPower(Integer transmissionPower) {
        return true;
    }

    public void setTransmissionPower(Integer transmissionPower) {
        this.transmissionPower = transmissionPower;
    }

    /**
     *  Returns The transmission power of the entity.
     * @return The transmission power of the entity.
     */
    
    
    public Integer getTransmissionPower() {
        return transmissionPower;
    }

    /**
     * A method for receiving a packet, which checks if it can detect the packet and then adds it to the reeived packets.
     * @param transmission The transmission to receiveTransmission.
     * @Effect if the package has a high enough transmission power, it is added using packetStrengthHighEnough().
     */
    public void receiveTransmission(LoraTransmission transmission) {
        if(packetStrengthHighEnough(transmission)){
            Boolean collision = false;
            for (LoraTransmission receivedTransmission: getAllReceivedTransmissions(getEnvironment().getNumberOfRuns()-1).keySet()) {
                if(collision(transmission,receivedTransmission)){
                    // Capture effect: a packet survives an overlap only if it is at least the threshold stronger.
                    double powerDifference = transmission.getTransmissionPower() - receivedTransmission.getTransmissionPower();
                    if (powerDifference < getTransmissionPowerThreshold()) {
                        collision = true;
                    }
                    if (powerDifference > -getTransmissionPowerThreshold()) {
                        this.receivedTransmissions.getLast().put(receivedTransmission,true);
                    }
                }
            }
            receivedTransmissions.getLast().put(transmission,collision);
            if(!collision){
                handleMacCommands(transmission.getContent());
                OnReceive(transmission.getContent().getPayload(), transmission.getContent().getSenderEUI(), transmission.getContent().getDesignatedReceiverEUI());

            }
        }
    }

    /**
     * A function for handling MAC commands.
     * @param packet the packets with MAC commands
     */
    public void handleMacCommands(LoraWanPacket packet){
        LinkedList<Byte> payload = new LinkedList<>(Arrays.asList(packet.getPayload()));
        LinkedList<Byte> variables = new LinkedList<>();
        for(MacCommand command : packet.getMacCommands()){
            for(Integer i = 0; i<command.getLength(); i++){
                variables.add(payload.get(i));
            }
            payload.removeAll(variables);
            switch (command) {
                case ResetInd:
                case ResetConf:
                case RekeyInd:
                case RekeyConf:
                case LinkADRAns:
                case LinkADRReq:
                case DevStatusAns:
                case DevStatusReq:
                case DlChannelAns:
                case DlChannelReq:
                case DutyCycleAns:
                case DutyCycleReq:
                case LinkCheckAns:
                case LinkCheckReq:
                case DeviceTimeAns:
                case DeviceTimeReq:
                case NewChannelAns:
                case NewChannelReq:
                case ForceRejoinReq:
                case RXParamSetupAns:
                case RXParamSetupReq:
                case TxParamSetupAns:
                case TxParamSetupReq:
                case ADRParamSetupAns:
                case ADRParamSetupReq:
                case RXTimingSetupAns:
                case RXTimingSetupReq:
                case RejoinParamSetupAns:
                case RejoinParamSetupReq:


            }
        }
    }

    /**
     * A method describing what the entity should do after successfully receiving a packet.
     * @param packet The received packet.
     * @param senderEUI The EUI of the sender
     * @param designatedReceiver The EUI designated receiver for the packet.
     */
    protected abstract void OnReceive(Byte[] packet, Long senderEUI, Long designatedReceiver);


    /**
     *  Returns The x-coordinate of the entity.
     * @return The x-coordinate of the entity.
     */
    
    
    public Integer getXPos() {
        return xPos;
    }

    /**
     * Checks if a new x-coordinate is valid and then sets that coordinate.
     * @param xPos The new x-coordinate.
     */
    
    public void setXPos(Integer xPos) {
        if(environment.isValidXpos(xPos)){
            this.xPos = xPos;
        }
    }

    /**
     *  Returns The y-coordinate of the entity.
     * @return The y-coordinate of the entity.
     */
    
    
        public Integer getYPos() {
            return yPos;
        }

    /**
     * Checks if a new y-coordinate is valid and then sets that coordinate.
     * @param yPos The new y-coordinate.
     * @Post    If the new y-coordinate is valid, the new coordinate is set.
     */
    
    public void setYPos(Integer yPos) {
        if(environment.isValidYpos(yPos)){
            this.yPos = yPos;
        }
    }

    /**
     * The spreading factor setting of the node.
     */
    private Integer SF = 12;

    /**
     * Checks if a given Spreading factor is valid
     * @param SF The spreading factor to check.
     * @return  If the spreading factor is valid
     */
    
    public static Boolean isValidSF(Integer SF){
        if(SF <= 12 && SF >= 7){
            return true;
        }
        else
            return false;
    }

    /**
     *  Returns The spreading factor.
     * @return The spreading factor.
     */
    
    
    public Integer getSF() {
        return SF;
    }

    /**
     * Checks if a spreading factor is valid and then sets it to the spreading factor.
     * @param SF the spreading factor to set.
     */
    public void setSF(Integer SF) {
        if(isValidSF(SF)) {
            this.SF = SF;
        }
    }

    @Getter
    Integer numberOfSentPackets = 0;

    @Getter
    Integer numberOfLostPackets = 0;

    /**
     * A method which sends a message to all gateways in the environment
     * @param message The message to send.
     * @return The transmissions that departed (one per receiver), empty if nothing was sent.
     */
    protected LinkedList<LoraTransmission> loraSend(LoraWanPacket message){
        LinkedList<LoraTransmission> packetsToSend = new LinkedList<>();
        if(!isTransmitting) {
            powerSettingHistory.getLast().add(new Pair<>(getEnvironment().getTime().toSecondOfDay(),getTransmissionPower()));
            spreadingFactorHistory.getLast().add(getSF());
            for (Gateway gateway : getEnvironment().getGateways()) {
                if (gateway != this)
                    packetsToSend.add(new LoraTransmission(this, gateway, getTransmissionPower(), 125, getSF(), message));
            }
            // Motes only send uplinks to gateways (LoRaWAN star topology).
            if (!(this instanceof Mote)) {
                for (Mote mote : getEnvironment().getMotes()) {
                    packetsToSend.add(new LoraTransmission(this, mote, getTransmissionPower(), 125, getSF(), message));
                }
            }
            sentTransmissions.getLast().add(packetsToSend.getFirst());
            for (LoraTransmission packet : packetsToSend) {
                packet.depart();
            }
            numberOfSentPackets++;
        }
        return packetsToSend;
    }

    /**
     * Checks if two packets collide according to the model: same spreading factor and overlapping in time.
     * Whether one of them survives (capture effect) is decided in receiveTransmission().
     * @param a The first packet.
     * @param b The second packet.
     * @return true if the packets collide, false otherwise.
     */
    public Boolean collision(LoraTransmission a, LoraTransmission b){
        if(a.getSpreadingFactor().equals(b.getSpreadingFactor())) {
            // Uses the time since the start: the LocalTime departure times repeat every simulated day.
            double centerDistance = Math.abs((a.getDepartureMillis() + a.getTimeOnAir() / 2)
                    - (b.getDepartureMillis() + b.getTimeOnAir() / 2));
            return centerDistance < a.getTimeOnAir() / 2 + b.getTimeOnAir() / 2;
        }
        return false;
    }

    /**
     * Checks if a transmission is strong enough to be received. The receiver sensitivity is the thermal noise
     * (-174 dBm/Hz over the bandwidth in Hz) plus a 6 dB noise figure plus the minimal SNR of the spreading factor
     * (-7.5 dB at SF7 down to -20 dB at SF12), i.e. -124.5 dBm at SF7 down to -137 dBm at SF12 for 125 kHz.
     * @param packet The received transmission.
     * @return true if the received power is above the sensitivity.
     */
    public Boolean packetStrengthHighEnough(LoraTransmission packet){
        return packet.getTransmissionPower() > -174 + 10 * Math.log10(packet.getBandwidth() * 1000.0) + 6 - (2.5 * packet.getSpreadingFactor() - 10);
    }

    /**
     * Returns the unique identifier.
     * @return the unique identifier.
     */
    public Long getEUI() {
        return EUI;
    }

    protected double calculateEnergyUsage(Integer transmissionPower, Double timeOnAir) {
        if (transmissionPower == null || timeOnAir == null) {
            return 0.0;
        }

        return Math.pow(10, ((double) transmissionPower) / 10) * timeOnAir / 1000;
    }

    public LinkedList<Double> getUsedEnergy(Integer run){
        ensureEnergyHistoryInitialized();

        if (run == null || run < 0 || run >= usedEnergyHistory.size()) {
            return new LinkedList<>();
        }

        return new LinkedList<>(usedEnergyHistory.get(run));
    }

    protected void recordEnergyUsage(int runIndex, double consumedEnergy) {
        if (runIndex < 0 || consumedEnergy <= 0) {
            return;
        }

        ensureEnergyHistoryInitialized();
        ensureEnergyHistoryCapacity(runIndex);

        usedEnergyHistory.get(runIndex).add(consumedEnergy);
        totalEnergyConsumedPerRun.set(runIndex, totalEnergyConsumedPerRun.get(runIndex) + consumedEnergy);
    }

    public double getTotalUsedEnergy(int runIndex) {
        ensureEnergyHistoryInitialized();

        if (runIndex < 0 || runIndex >= totalEnergyConsumedPerRun.size()) {
            return 0.0;
        }

        return totalEnergyConsumedPerRun.get(runIndex);
    }

    private void ensureEnergyHistoryCapacity(int runIndex) {
        ensureEnergyHistoryInitialized();

        while (runIndex >= usedEnergyHistory.size()) {
            usedEnergyHistory.add(new LinkedList<>());
        }

        while (runIndex >= totalEnergyConsumedPerRun.size()) {
            totalEnergyConsumedPerRun.add(0.0);
        }
    }

    private void ensureEnergyHistoryInitialized() {
        if (usedEnergyHistory == null) {
            usedEnergyHistory = new LinkedList<>();
        }
        if (usedEnergyHistory.isEmpty()) {
            usedEnergyHistory.add(new LinkedList<>());
        }

        if (totalEnergyConsumedPerRun == null) {
            totalEnergyConsumedPerRun = new LinkedList<>();
        }
        if (totalEnergyConsumedPerRun.isEmpty()) {
            totalEnergyConsumedPerRun.add(0.0);
        }
    }

    /**
     * Resets the received and sent transmissions and the power setting and the spreading factor history of the entity.
     */
    public void reset(){
        ensureEnergyHistoryInitialized();
        powerSettingHistory.clear();
        powerSettingHistory.add(new LinkedList<>());
        spreadingFactorHistory.clear();
        spreadingFactorHistory.add(new LinkedList<>());
        receivedTransmissions.clear();
        receivedTransmissions.add(new LinkedHashMap<>());
        sentTransmissions.clear();
        sentTransmissions.add(new LinkedList<>());
        usedEnergyHistory.clear();
        usedEnergyHistory.add(new LinkedList<>());
        totalEnergyConsumedPerRun.clear();
        totalEnergyConsumedPerRun.add(0.0);
    }

    /**
     * Adds a new list to the received and sent transmissions and the power setting and the spreading factor history of the entity.
     */
    public void addRun(){
        ensureEnergyHistoryInitialized();
        powerSettingHistory.add(new LinkedList<>());
        spreadingFactorHistory.add(new LinkedList<>());
        receivedTransmissions.add(new LinkedHashMap<>());
        sentTransmissions.add(new LinkedList<>());
        usedEnergyHistory.add(new LinkedList<>());
        totalEnergyConsumedPerRun.add(0.0);
    }

    /**
     * Returns if the entity is enabled in this run.
     * @return If the entity is enabled in this run.
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the entity.
     * @param enabled If the entity is enabled.
     */
    public void enable(Boolean enabled) {
        this.enabled = enabled;
    }
}
