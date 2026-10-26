package Simulation;

import IotDomain.*;
import SelfAdaptation.Instrumentation.MoteProbe;
import models.SimulationState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class provides the data and functionality of the main simulation.
 * @version 1.0
 */
public class MainSimulation extends Thread {
    private static final MoteProbe moteProbe = new MoteProbe();

    /**
     * The current environment of the simulation.
     * @since 1.0
     */
    SimulationState simulationState;

    /**
     * The seed for the random number generators of the simulation, or {@code null} for an unseeded run.
     */
    private final Long seed;

    /**
     * Constructs a {@code MainSimulation} object with the Environment {@code environment}.
     * @param simulationState The environment of the {@code MainSimulation} object.
     * @since 1.0
     */
    public MainSimulation(SimulationState simulationState) {
        this(simulationState, null);
    }

    /**
     * Constructs a {@code MainSimulation} object whose random values are determined by {@code seed}.
     * @param simulationState The environment of the {@code MainSimulation} object.
     * @param seed The seed for the random number generators, or {@code null} for an unseeded run.
     */
    public MainSimulation(SimulationState simulationState, Long seed) {
        this.simulationState = simulationState;
        this.seed = seed;
    }

    public static IotDomain.Environment createEnvironment(Long seed) {
        /*
         * Generate all the points
         */
        GeoPosition mapzero = new GeoPosition(50.853718, 4.673155);
        Integer mapsize = (int) Math.ceil(1000 *Math.max(IotDomain.Environment.distance(50.853718, 4.673155, 50.878697,   4.673155), IotDomain.Environment.distance(50.853718, 4.673155, 50.853718,   4.701200)));
        GeoPosition leuven = new GeoPosition(50,51,46,4,41,2);
        GeoPosition gw1 = new GeoPosition(50.859722, 4.681944);
        GeoPosition gw2 = new GeoPosition(50.863780, 4.677992);
        GeoPosition gw3 = new GeoPosition(50.867222, 4.678056);
        GeoPosition gw4 = new GeoPosition(50.856667, 4.676389);

        GeoPosition wp1 = new GeoPosition(50.856020, 4.675844);
        GeoPosition wp2 = new GeoPosition(50.856545, 4.676743);
        GeoPosition wp3 = new GeoPosition(50.857852, 4.679702);
        GeoPosition wp4 = new GeoPosition(50.860061, 4.683473);
        GeoPosition wp5 = new GeoPosition(50.861985, 4.680993);
        GeoPosition wp6 = new GeoPosition(50.862263, 4.680672);
        GeoPosition wp7 = new GeoPosition(50.862696, 4.680416);
        GeoPosition wp8 = new GeoPosition(50.863049, 4.680321);
        GeoPosition wp9 = new GeoPosition(50.863455, 4.680385);
        GeoPosition wp10 = new GeoPosition(50.863977, 4.680610);
        GeoPosition wp11 = new GeoPosition(50.864770, 4.680898);
        GeoPosition wp12 = new GeoPosition(50.865176, 4.680973);
        GeoPosition wp13 = new GeoPosition(50.865583, 4.680976);
        GeoPosition wp14 = new GeoPosition(50.867980, 4.680381);
        GeoPosition wp15 = new GeoPosition(50.867881, 4.678226);
        GeoPosition wp16 = new GeoPosition(50.868028, 4.678175);
        GeoPosition wp17 = new GeoPosition(50.869650, 4.676740);

        GeoPosition wp21 = new GeoPosition(50.868551, 4.698337);
        GeoPosition wp22 = new GeoPosition(50.866713, 4.695153);
        GeoPosition wp23 = new GeoPosition(50.861330, 4.685687);
        GeoPosition wp24 = new GeoPosition(50.857910, 4.679724);
        GeoPosition wp25 = new GeoPosition(50.856486, 4.676650);

        /*
         * Create tracks.
         */
        LinkedList<GeoPosition> track0 = new LinkedList<>(Arrays.asList(wp1,wp2,wp3,wp4,wp5,wp6,wp7,wp8,wp9,wp10,wp11,wp12,wp13,wp14,wp15,wp16,wp17));
        LinkedList<GeoPosition> track2 = new LinkedList<>(Arrays.asList(wp21,wp22,wp23,wp24,wp25,wp1));

        GeoPosition positionMote2 = new GeoPosition(50.862752, 4.688886);

        /*
         * Prepare simulation environment.
         */
        Characteristic[][] map = new Characteristic[mapsize][mapsize];
        for(int i =0; i < mapsize; i++){
            for(int j =0; j < mapsize / 3 ; j++){
                map[j][i] = Characteristic.Forest;
            }
            for(int j = mapsize / 3; j < 2 * mapsize / 3 ; j++){
                map[j][i] = Characteristic.Plain;
            }

            for(int j = 2 * mapsize / 3; j < mapsize ; j++){
                map[j][i] = Characteristic.City;
            }
        }

        IotDomain.Environment environment = new IotDomain.Environment(map,mapzero,new LinkedHashSet<>());
        if (seed != null) {
            environment.setSeed(seed);
        }

        /*
        Add motes and gateways.
         */
        Random random = environment.getRandom();
        new Gateway(random.nextLong(),(int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), gw1.getLongitude())),
                (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),gw1.getLatitude(), mapzero.getLongitude())),
                environment, 14,12);
        new Gateway(random.nextLong(),(int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), gw2.getLongitude())),
                (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),gw2.getLatitude(), mapzero.getLongitude())),
                environment, 14,12);
        new Gateway(random.nextLong(),(int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), gw3.getLongitude())),
                (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),gw3.getLatitude(), mapzero.getLongitude())),
                environment, 14,12);
        new Gateway(random.nextLong(),(int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), gw4.getLongitude())),
                (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),gw4.getLatitude(), mapzero.getLongitude())),
                environment, 14,12);


        /*
         * Mote 0
         */
        new Mote(random.nextLong(),
                (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), wp1.getLongitude())),
                (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),wp1.getLatitude(), mapzero.getLongitude())),
                environment, 14,12, new LinkedList<>(),0, track0,10,0.5);

        /*
         * Mote 1
         */
        new Mote(random.nextLong(),toMapXCoordinate(wp21,mapzero),
                toMapYCoordinate(wp21,mapzero),
                environment, 14,12, new LinkedList<>(),0, new LinkedList<>(),10,0.5);

        /*
         * Mote 2
         */
        new Mote(random.nextLong(),
                (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), positionMote2.getLongitude())),
                (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),positionMote2.getLatitude(), mapzero.getLongitude())),
                environment, 14,12, new LinkedList<>(),0, track2,10,0.5);

        return environment;
    }

    /**
     * Starts a DingNet simulation with three motes and four gateways. Two motes are moved along different paths.
     * The simulation state is updated regularly.
     * @exception InterruptedException can occur in {@link Thread#sleep(long)}
     * @since 1.0
     */
    public void runSimulation(boolean visualizeResults) throws InterruptedException {
        /*
        Set to enable or disable adaptation of node 0 (D1).
         */
        Boolean adaption = false;

        Environment environment = createEnvironment(seed);

        /*
         Get the motes.
         */
        Mote mote0 = environment.getMotes().get(0);
        Mote mote1 = environment.getMotes().get(1);
        Mote mote2 = environment.getMotes().get(2);

        LinkedList<GeoPosition> track0 = mote0.getPath();
        LinkedList<GeoPosition> track2 = mote2.getPath();

        this.simulationState.setEnvironment(environment);

        /*
         Actual simulation
         */
        Random random = environment.getRandom();
        LinkedList<Integer> powerSetting0 = new LinkedList<>();
        LinkedList<LoraTransmission> highestPower0 = new LinkedList<>();
        int mote0Counter = mote0.getSamplingRate() - 1;
        int mote1Counter = mote1.getSamplingRate() - 1;
        Integer mote2counter = random.nextInt(15)+1;
        LinkedList<Integer> indexesMote2 = new LinkedList<>();
        indexesMote2.add(mote2counter);
        int trackPosition0 = 0;
        int trackPosition2 = 0;
        GeoPosition mapzero = environment.getMapCenter();

        while(!simulationState.getShouldStop()) {
            // Update the position of mote0
            if(moveMote(track0.get(trackPosition0 % track0.size()), mote0, mapzero)){
                if(mote0Counter == 0) {
                    mote0.sendToGateWay(new Byte[0], new HashMap<>());
                    if(adaption){
                        powerSetting0.add(mote0.getTransmissionPower());
                        highestPower0.add(naiveAdaptionAlgorithm(mote0));
                    }

                    updateMoteStatistics(mote0, environment);
                    mote0Counter = mote0.getSamplingRate() - 1;
                }
                else
                    mote0Counter --;
                // Mote didn't move, reached a waypoint
            } else {
                trackPosition0++;
            }

            if (mote1Counter == 0) {
                mote1.sendToGateWay(new Byte[0], new HashMap<>());

                updateMoteStatistics(mote1, environment);

                mote1Counter = mote1.getSamplingRate() - 1;
            } else {
                mote1Counter--;
            }

            // Update the position of mote2
            if (moveMote(track2.get(trackPosition2 % track2.size()), mote2, mapzero)) {
                if (mote2counter == 0) {
                    mote2.sendToGateWay(new Byte[0], new HashMap<>());

                    updateMoteStatistics(mote2, environment);

                    mote2counter = random.nextInt(15) + 1;
                    indexesMote2.add(indexesMote2.getLast() + mote2counter);
                } else
                    mote2counter--;
                // Mote didn't move, reached a waypoint
            } else {
                trackPosition2++;
            }

            environment.tick(1500);
        }

        if (visualizeResults)
            showCharts(environment, indexesMote2);
    }

    private void updateMoteStatistics(Mote mote, Environment environment) {
        mote.setHighestReceivedSignal(moteProbe.getHighestReceivedSignal(mote));
        mote.setShortestDistanceToGateway(moteProbe.getShortestDistanceToGateway(mote));
        mote.setPacketLoss(mote.calculatePacketLoss(environment.getNumberOfRuns() - 1));
    }

    private static void showCharts(Environment environment, List<Integer> indexesMote2) {
        /*
         * Data collection mote 0
         */
        LinkedList<LinkedList<LoraTransmission>> transmissionsMote0 = new LinkedList<>();
        int transmittedPacketsMote0 = 0;
        int lostPacketsMote0 = 0;
        for(Gateway gateway : environment.getGateways()){
            transmissionsMote0.add(new LinkedList<>());
            for(LoraTransmission transmission :gateway.getAllReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).keySet()){
                if(transmission.getSender() == environment.getMotes().get(0)) {
                    transmittedPacketsMote0++;
                    if (!gateway.getAllReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).get(transmission))
                        transmissionsMote0.getLast().add(transmission);
                    else {
                        transmissionsMote0.getLast().add(new LoraTransmission(transmission.getSender(),
                                transmission.getReceiver(), -10, transmission.getBandwidth(),
                                transmission.getSpreadingFactor(), transmission.getContent()));
                        lostPacketsMote0++;
                    }
                }
            }
        }

        /*
       Data collection mote 2
        */
        LinkedList<LinkedList<LoraTransmission>> transmissionsMote2 = new LinkedList<>();

        int transmittedPacketsMote2 = 0;
        int lostPacketsMote2 = 0;
        for(Gateway gateway : environment.getGateways()){
            transmissionsMote2.add(new LinkedList<>());
            for(LoraTransmission transmission :gateway.getAllReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).keySet()){
                if(transmission.getSender() == environment.getMotes().get(2)) {
                    transmittedPacketsMote2 ++;
                    if (!gateway.getAllReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).get(transmission))
                        transmissionsMote2.getLast().add(transmission);
                    else {
                        lostPacketsMote2 ++;
                        transmissionsMote2.getLast().add(new LoraTransmission(transmission.getSender(),
                                transmission.getReceiver(), -10, transmission.getBandwidth(),
                                transmission.getSpreadingFactor(), transmission.getContent()));
                    }
                }
            }
        }

        System.out.println("Sent Packets: " + transmittedPacketsMote0);
        System.out.println("Lost Packets: " + lostPacketsMote0);
        System.out.println("Sent Packets: " + transmittedPacketsMote2);
        System.out.println("Lost Packets: " + lostPacketsMote2);

        /*
         * Creating charts
         */
        XYSeriesCollection dataMote0 = new XYSeriesCollection();
        for(LinkedList<LoraTransmission> list : transmissionsMote0){
            XYSeries series = new XYSeries(list.get(0).getReceiver().toString());
            Integer i = 0;
            for (LoraTransmission transmission: list){
                series.add(i,(Number)transmission.getTransmissionPower());
                i = i +10;
            }
            dataMote0.addSeries(series);
        }

        XYSeriesCollection dataMote2 = new XYSeriesCollection();
        for(LinkedList<LoraTransmission> list: transmissionsMote2){
            XYSeries series = new XYSeries(list.get(0).getReceiver().toString());
            int i = 0;
            for (LoraTransmission transmission: list){
                series.add(indexesMote2.get(i),(Number)transmission.getTransmissionPower());
                i++;
            }
            dataMote2.addSeries(series);
        }

        JFreeChart receivedPowerChartMote0 = ChartFactory.createScatterPlot(
                null, // chart title
                "Distance travelled in meter", // x axis label
                "Received signal strength in dB", // y axis label
                dataMote0, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        XYPlot xyPlotreceivedPowerMote0 = (XYPlot) receivedPowerChartMote0.getPlot();
        xyPlotreceivedPowerMote0.setDomainCrosshairVisible(true);
        xyPlotreceivedPowerMote0.setRangeCrosshairVisible(true);
        NumberAxis domainreceivedPowerMote0 = (NumberAxis) xyPlotreceivedPowerMote0.getDomainAxis();
        domainreceivedPowerMote0.setRange(0.0, 2700.0);
        domainreceivedPowerMote0.setTickUnit(new NumberTickUnit(200));
        domainreceivedPowerMote0.setVerticalTickLabels(true);
        NumberAxis rangereceivedPowerMote0 = (NumberAxis) xyPlotreceivedPowerMote0.getRangeAxis();
        rangereceivedPowerMote0.setRange(-85, 0.0);
        rangereceivedPowerMote0.setTickUnit(new NumberTickUnit(4));

        JFreeChart receivedPowerChartMote2 = ChartFactory.createScatterPlot(
                null, // chart title
                "Distance travelled in meter", // x axis label
                "Received signal strength in dB", // y axis label
                dataMote2, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        XYPlot xyPlotreceivedPowerMote2 = (XYPlot) receivedPowerChartMote2.getPlot();
        xyPlotreceivedPowerMote2.setDomainCrosshairVisible(true);
        xyPlotreceivedPowerMote2.setRangeCrosshairVisible(true);
        NumberAxis domainreceivedPowerMote2 = (NumberAxis) xyPlotreceivedPowerMote2.getDomainAxis();
        domainreceivedPowerMote2.setRange(0.0, 2700.0);
        domainreceivedPowerMote2.setTickUnit(new NumberTickUnit(200));
        domainreceivedPowerMote2.setVerticalTickLabels(true);
        NumberAxis rangereceivedPowerMote2 = (NumberAxis) xyPlotreceivedPowerMote2.getRangeAxis();
        rangereceivedPowerMote2.setRange(-85, 0.0);
        rangereceivedPowerMote2.setTickUnit(new NumberTickUnit(4));

        JFrame frame1 = new JFrame("received signals");
        ChartPanel HighestSignalChartpanel = new ChartPanel(receivedPowerChartMote0);
        HighestSignalChartpanel.setPreferredSize(new java.awt.Dimension(1000, 500));
        frame1.getContentPane().add(HighestSignalChartpanel, BorderLayout.NORTH);
        ChartPanel powersettingChart1panel = new ChartPanel(receivedPowerChartMote2);
        powersettingChart1panel.setPreferredSize(new java.awt.Dimension(1000, 500));
        frame1.getContentPane().add( powersettingChart1panel, BorderLayout.SOUTH);
        frame1.pack();
        frame1.setVisible(true);
    }

    /**
     * Runs the simulation and sets the {@code isRunning} flag of the simulation state to {@code false} again.
     * @exception RuntimeException can occur in {@link MainSimulation#runSimulation(boolean)}
     * @since 1.0
     */
    public void run() {
        try {
            this.runSimulation(false);
            this.simulationState.setIsRunning(false);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /*
    The naïve adaptation for our paper
     */
    private static LinkedList<Double> algorithmBuffer = new LinkedList<>();
    private static LoraTransmission naiveAdaptionAlgorithm(Mote mote){
        LinkedList<LoraTransmission> lastTransmissions = new LinkedList<>();
        for(Gateway gateway :mote.getEnvironment().getGateways()){
            Boolean placed = false;
            for(int i = gateway.getReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).size()-1; i>=0 && !placed; i--) {
                if(gateway.getReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).get(i).getSender() == mote) {
                    lastTransmissions.add(gateway.getReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).get(i));
                    placed = true;
                }
            }
        }
        LoraTransmission bestTransmission = lastTransmissions.getFirst();
        for (LoraTransmission transmission : lastTransmissions){
            if(transmission.getTransmissionPower() > bestTransmission.getTransmissionPower())
                bestTransmission = transmission;
        }
        algorithmBuffer.add(bestTransmission.getTransmissionPower());
        if(algorithmBuffer.size() ==5){
            double average = 0;
            for (Double power : algorithmBuffer){
                average+= power;
            }
            average = average /5;
            if(average > -42) {
                if (mote.getTransmissionPower() > -3)
                    mote.setTransmissionPower(mote.getTransmissionPower() - 1);
            }
            if(average < -48){
                if(mote.getTransmissionPower() < 14)
                    mote.setTransmissionPower(mote.getTransmissionPower() +1);
            }
            algorithmBuffer = new LinkedList<>();
        }
        return bestTransmission;
    }

    /**
     * A function that moves a mote to a geoposition 1 step and returns if the note has moved.
     * @param position
     * @param mote
     * @param mapzero
     * @return If the node has moved
     */
    private static Boolean moveMote(GeoPosition position, Mote mote, GeoPosition mapzero){
        Integer xPos = toMapXCoordinate(position, mapzero);
        Integer yPos = toMapYCoordinate(position, mapzero);
        if(Integer.signum(xPos - mote.getXPos()) != 0 || Integer.signum(yPos - mote.getYPos()) != 0){
            if(Math.abs(mote.getXPos() - xPos) >= Math.abs(mote.getYPos() - yPos)){
                mote.setXPos(mote.getXPos() + Integer.signum(xPos - mote.getXPos()));
            }  else {
                mote.setYPos(mote.getYPos() + Integer.signum(yPos - mote.getYPos()));
            }
            return true;
        }
        return false;
    }

    private static Integer toMapXCoordinate(GeoPosition geoPosition, GeoPosition mapzero){
        return (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), geoPosition.getLongitude()));
    }

    private static Integer toMapYCoordinate(GeoPosition geoPosition, GeoPosition mapzero){
        return (int)Math.round(1000* IotDomain.Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),geoPosition.getLatitude(), mapzero.getLongitude()));
    }

    public static void main(String[] args) throws InterruptedException {
        new MainSimulation(new SimulationState()).runSimulation(true);
    }
}