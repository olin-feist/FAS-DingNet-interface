package IotDomain;


import lombok.Getter;
import lombok.Setter;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


/**
 * A class representing the energy bound and moving motes in the network.
 */
public class Mote extends NetworkEntity {
    /**
     * Returns the mote sensors of the mote.
     * @return The mote sensors of the mote.
     */

    public LinkedList<MoteSensor> getSensors() {
        return moteSensors;
    }

    /**
     * A LinkedList MoteSensors representing all sensors on the mote.
     */

    private LinkedList<MoteSensor> moteSensors = new LinkedList<>();
    /**
     * A LinkedList of GeoPositions representing the path the mote will follow.
     */

    private LinkedList<GeoPosition> path;

    /**
     * An integer representing the energy level of the mote.
     */

    private Integer energyLevel;

    /**
     * Accumulates fractional energy usage until a full energy unit can be deducted from the mote.
     */
    private double energyConsumptionBuffer = 0.0;

    /**
     * The transmissions (one per gateway) of the latest packet sent by the mote.
     */
    private LinkedList<LoraTransmission> latestTransmissions = new LinkedList<>();

    /**
     * The number of settled packets that at least one gateway received without collision, and that none did.
     */
    private int deliveredPackets = 0;
    private int undeliveredPackets = 0;

    /**
     * Whether each of the most recently settled packets was delivered, oldest first.
     * Synchronized on itself: the simulation thread writes it while the HTTP thread reads it.
     */
    private final LinkedList<Boolean> recentDeliveries = new LinkedList<>();
    private static final int MAX_RECENT_DELIVERIES = 100;
    /**
     * An integer representing the sampling rate of the mote.
     */

    private Integer samplingRate;
    /**
     * An integer representing the number of requests for data of the mote.
     */

    private Integer numberOfRequests;
    /**
     * A Double representing the movement speed of the mote.
     */

    private Double movementSpeed;
    /**
     * An integer representing the start offset of the mote.
     */

    private Integer startOffset;

    /**
     * A constructor generating a node with a given x-coordinate, y-coordinate, environment, transmitting power
     * spreading factor, list of MoteSensors, energy level, path, sampling rate, movement speed and start offset.
     * @param DevEUI The device's unique identifier
     * @param xPos  The x-coordinate of the node.
     * @param yPos  The y-coordinate of the node.
     * @param environment   The environment of the node.
     * @param SF    The spreading factor of the node.
     * @param transmissionPower The transmitting power of the node.
     * @param moteSensors The mote sensors for this mote.
     * @param energyLevel The energy level for this mote.
     * @param path The path for this mote to follow.
     * @param samplingRate The sampling rate of this mote.
     * @param movementSpeed The movement speed of this mote.
     * @param startOffset The start offset of this mote.
     */

    public Mote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower,
                Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, LinkedList<GeoPosition> path, Integer samplingRate, Double movementSpeed, Integer startOffset){
       // 6 dB co-spreading-factor capture threshold (Bor et al., "Do LoRa Low-Power Wide-Area Networks Scale?", 2016)
       super(DevEUI, xPos,yPos, environment,transmissionPower,SF,6.0);
        environment.addMote(this);
        OverTheAirActivation();
        this.moteSensors = moteSensors;
        this.path = path;
        this.energyLevel = energyLevel;
        this.samplingRate = samplingRate;
        numberOfRequests = samplingRate;
        this.movementSpeed = movementSpeed;
        this.startOffset = startOffset;

    }

    /**
     * A constructor generating a node with a given x-coordinate, y-coordinate, environment, transmitting power
     * spreading factor, list of MoteSensors, energy level, path, sampling rate and movement speed and  random start offset.
     * @param DevEUI The device's unique identifier
     * @param xPos  The x-coordinate of the node.
     * @param yPos  The y-coordinate of the node.
     * @param environment   The environment of the node.
     * @param SF    The spreading factor of the node.
     * @param transmissionPower The transmitting power of the node.
     * @param moteSensors The mote sensors for this mote.
     * @param energyLevel The energy level for this mote.
     * @param path The path for this mote to follow.
     * @param samplingRate The sampling rate of this mote.
     * @param movementSpeed The movement speed of this mote.
     */

    public Mote(Long DevEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower,
                Integer SF, LinkedList<MoteSensor> moteSensors, Integer energyLevel, LinkedList<GeoPosition> path, Integer samplingRate, Double movementSpeed){
        this(DevEUI,xPos,yPos, environment,transmissionPower,SF,moteSensors,energyLevel,path,samplingRate, movementSpeed,Math.abs((new Random()).nextInt(5)));
    }

    /**
     * A method describing what the mote should do after successfully receiving a packet.
     * @param packet The received packet.
     * @param senderEUI The EUI of the sender
     * @param designatedReceiver The EUI designated receiver for the packet.
     */
    @Override
    protected void OnReceive(Byte[] packet, Long senderEUI, Long designatedReceiver) {

    }

    /**
     * a function for the OTAA protocol.
     */
    public void OverTheAirActivation(){
    }

    /**
     * Returns the path of the mote.
     * @return The path of the mote.
     */

    public LinkedList<GeoPosition> getPath() {
        return path;
    }

    /**
     * Sets the path of the mote to a given path.
     * @param path The path to set.
     */

    public void setPath(LinkedList<GeoPosition> path) {
        this.path = path;
    }

    /**
     * A function for sending a message with MAC commands to the gateways.
     * @param data The data to send in the message
     * @param macCommands the MAC commands to include in the message.
     */
    public void sendToGateWay(Byte[] data, HashMap<MacCommand,Byte[]> macCommands){
        Byte[] payload = new Byte[data.length+macCommands.size()];
        int i = 0;
        for(MacCommand key : macCommands.keySet()){
            for(Byte dataByte : macCommands.get(key)){
            payload[i] = dataByte;
            i++;
            }
        }
        for(int j =0; j< data.length;j++){
            payload[i] = data[j];
            i++;
        }

        LoraWanPacket packet = new LoraWanPacket(getEUI(), (long) 1,payload, new LinkedList<>(macCommands.keySet()));
        loraSend(packet);
    }

    @Override
    protected LinkedList<LoraTransmission> loraSend(LoraWanPacket message){
        int runIndex = 0;
        if (getEnvironment() != null && getEnvironment().getNumberOfRuns() > 0) {
            runIndex = getEnvironment().getNumberOfRuns() - 1;
        }

        settleLatestPacket(runIndex);

        int transmissionsBeforeSend = getSentTransmissions(runIndex).size();

        LinkedList<LoraTransmission> transmissions = super.loraSend(message);

        applyEnergyConsumptionForNewTransmissions(runIndex, transmissionsBeforeSend);
        latestTransmissions = transmissions;
        return transmissions;
    }

    /**
     * Records whether the latest packet was delivered, i.e. received without collision by at least one gateway.
     * This is called right before the next packet is sent: the latest packet is off air by then (its airtime is
     * shorter than the time between two packets of a mote), so its outcome can no longer change.
     * @param runIndex The index of the current run.
     */
    private void settleLatestPacket(int runIndex) {
        if (latestTransmissions.isEmpty()) {
            return;
        }
        boolean delivered = latestTransmissions.stream().anyMatch(transmission -> wasReceived(transmission, runIndex));
        if (delivered) {
            deliveredPackets++;
        } else {
            undeliveredPackets++;
        }
        synchronized (recentDeliveries) {
            recentDeliveries.add(delivered);
            if (recentDeliveries.size() > MAX_RECENT_DELIVERIES) {
                recentDeliveries.removeFirst();
            }
        }
        numberOfLostPackets = undeliveredPackets;
        latestTransmissions = new LinkedList<>();
    }

    /**
     * Returns if a transmission was received by its receiver without collision.
     * @param transmission The transmission.
     * @param run The index of the run.
     * @return true if the receiver received the transmission and it did not collide.
     */
    public static boolean wasReceived(LoraTransmission transmission, int run) {
        Boolean collided = transmission.getReceiver().getAllReceivedTransmissions(run).get(transmission);
        return collided != null && !collided;
    }

    /**
     * Returns the transmissions (one per gateway) of the latest packet sent by the mote.
     * @return The transmissions of the latest packet, empty if the mote has not sent anything yet.
     */
    public LinkedList<LoraTransmission> getLatestTransmissions() {
        return latestTransmissions;
    }

    private void applyEnergyConsumptionForNewTransmissions(int runIndex, int transmissionsBeforeSend) {
        LinkedList<LoraTransmission> sentTransmissionsForRun = getSentTransmissions(runIndex);
        if (sentTransmissionsForRun.size() <= transmissionsBeforeSend) {
            return;
        }

        List<Pair<Integer, Integer>> powerHistory = getPowerSettingHistory(runIndex);

        for (int index = transmissionsBeforeSend; index < sentTransmissionsForRun.size(); index++) {
            if (index >= powerHistory.size()) {
                break;
            }

            LoraTransmission transmission = sentTransmissionsForRun.get(index);
            Pair<Integer, Integer> powerEntry = powerHistory.get(index);
            double consumedEnergy = calculateEnergyUsage(powerEntry.getRight(), transmission.getTimeOnAir());
            recordEnergyUsage(runIndex, consumedEnergy);
            if (getEnergyLevel() != null) {
                decreaseEnergyLevel(consumedEnergy);
            }
        }
    }

    private void decreaseEnergyLevel(double consumedEnergy) {
        if (consumedEnergy <= 0) {
            return;
        }

        energyConsumptionBuffer += consumedEnergy;

        int wholeUnitsToConsume = (int) Math.floor(energyConsumptionBuffer);
        if (wholeUnitsToConsume <= 0) {
            return;
        }

        energyLevel = Math.max(0, energyLevel - wholeUnitsToConsume);
        energyConsumptionBuffer -= wholeUnitsToConsume;
    }

    /**
     * Returns the energy level of the mote.
     * @return The energy level of the mote.
     */

    public Integer getEnergyLevel(){
        return this.energyLevel;
    }

    /**
     * Sets the energy level of the mote.
     * @param energyLevel The energy level to set.
     */

    public void setEnergyLevel(Integer energyLevel) {
        this.energyLevel = energyLevel;
    }

    /**
     * Sets the mote sensors of the mote.
     * @param moteSensors the mote sensors to set.
     */

    public void setSensors(LinkedList<MoteSensor> moteSensors) {
        this.moteSensors = moteSensors;
    }

    /**
     * Returns the sampling rate of the mote.
     * @return The sampling rate of the mote.
     */

    public Integer getSamplingRate() {
        return samplingRate;
    }

    /**
     * Returns the number of requests for data.
     * @return The number of requests for data.
     */

    public Integer getNumberOfRequests() {
        return numberOfRequests;
    }

    /**
     * Sets the sampling rate of the mote.
     * @param samplingRate The sampling rate of the mote
     */

    public void setSamplingRate(Integer samplingRate){
        this.samplingRate = samplingRate;
        setNumberOfRequests(getSamplingRate());
    }

    /**
     * Sets the number of requests for data.
     * @param numberOfRequests The number of requests for data.
     */

    private void setNumberOfRequests(Integer numberOfRequests) {
        this.numberOfRequests = numberOfRequests;
    }

    /**
     * Returns if a mote should send data on this request.
     * @return true if the number of request since last answer is the sampling rate.
     * @return false otherwise.
     */
    public boolean shouldSend(){
        if(getNumberOfRequests() == 0){
            setNumberOfRequests(getSamplingRate());
            return true;
        }
        else{
            setNumberOfRequests(getNumberOfRequests()-1);
            return false;
        }
    }

    /**
     * Returns the movementSpeed of the mote.
     * @return The movementSpeed of the mote.
     */

    public Double getMovementSpeed() {
        return movementSpeed;
    }

    /**
     * Sets the movement speed of the mote.
     * @param movementSpeed The movement speed of the mote.
     */

    public void setMovementSpeed(Double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    /**
     * Returns the start offset of the mote.
     * @return the start offset of the mote.
     */

    public Integer getStartOffset(){
        return this.startOffset;
    }

    @Getter
    @Setter
    private Double shortestDistanceToGateway;

    @Getter
    @Setter
    private Double highestReceivedSignal;

    @Getter
    @Setter
    private Double packetLoss;

    @Override
    public void reset(){
        super.reset();
        energyConsumptionBuffer = 0.0;
        clearDeliveries();
    }

    @Override
    public void addRun(){
        super.addRun();
        clearDeliveries();
    }

    private void clearDeliveries() {
        latestTransmissions = new LinkedList<>();
        deliveredPackets = 0;
        undeliveredPackets = 0;
        synchronized (recentDeliveries) {
            recentDeliveries.clear();
        }
        numberOfLostPackets = 0;
    }

    /**
     * Calculates the packet loss of the mote: the fraction of its settled packets that no gateway received
     * without collision. The latest packet is settled when the next one is sent.
     * @param run the run index (the current run is tracked).
     * @return packet loss ratio, or 0 when no packet has been settled yet.
     */
    public Double calculatePacketLoss(Integer run) {
        int settledPackets = deliveredPackets + undeliveredPackets;
        if (settledPackets == 0) {
            return 0D;
        }
        return undeliveredPackets / (double) settledPackets;
    }

    /**
     * Calculates the packet loss for the most recent packets of the mote.
     *
     * @param run        the run index (the current run is tracked).
     * @param windowSize number of recent packets to include in the calculation.
     * @return packet loss ratio for the selected window, or 0 when no packet has been settled yet.
     */
    public Double calculateRecentPacketLoss(Integer run, int windowSize) {
        synchronized (recentDeliveries) {
            if (windowSize <= 0 || recentDeliveries.isEmpty()) {
                return 0D;
            }

            int inspected = 0;
            int lost = 0;
            Iterator<Boolean> deliveries = recentDeliveries.descendingIterator();
            while (deliveries.hasNext() && inspected < windowSize) {
                if (!deliveries.next()) {
                    lost++;
                }
                inspected++;
            }
            return lost / (double) inspected;
        }
    }
}
