package IotDomain;



import java.awt.*;


/**
 * A class representing the characteristics of a certain location.
 */
public enum Characteristic{

    /*
     * Log-distance path loss: PL(d) = referencePathLoss + 10 * pathLossExponent * log10(d / referenceDistance).
     * Plain uses LoRa 868 MHz measurements (Petajajarvi et al., 2015: PL(d) = 128.95 + 23.2 log10(d / 1 km), sigma 7.8 dB).
     * Forest and City are illustrative values with more attenuation and can be tuned.
     * The shadow fading is applied per packet, so it is kept smaller than the measured location-to-location variation.
     */
    Forest(3.0,1000,138.0,4.0, new Color(0,102,0)), City(2.8,1000,135.0,4.0,new Color(51,153,255)),Plain(2.32,1000,128.95,3.0,new Color(255,153,0));
    /**
     * An integer representing the path loss exponent in a certain position.
     */
    private final double pathLossExponent;
    /**
     * An integer representing the reference distance for the mean path loss in a certain position.
     */
    private final double referenceDistance;
    /**
     * The mean path loss (dB) at the reference distance.
     */
    private final double referencePathLoss;
    /**
     * An integer representing the shadow fading in a certain position.
     */
    private final double shadowFading;
    /**
     * The color of the characteristic.
     */
    private final Color color;

    /**
     * A constructor generating a characteristic with a given mean path loss, path loss exponent, reference distance
     * and shadow fading.
     * @param pathLossExponent  The path loss exponent to set.
     * @param referenceDistance The reference distance to set.
     * @param referencePathLoss The mean path loss at the reference distance to set.
     * @param shadowFading The shadow fading to set.
     * @param color The color of the characteristic.
     */
     Characteristic(double pathLossExponent, double referenceDistance, double referencePathLoss, double shadowFading, Color color) {
         this.color = color;
        this.pathLossExponent = pathLossExponent;
        this.referenceDistance = referenceDistance;
        this.referencePathLoss = referencePathLoss;
        this.shadowFading = shadowFading;
    }


    /**
     *  Returns the path loss exponent of this position.
     * @return The path loss exponent of this position.
     */
    
    public double getPathLossExponent() {
        return pathLossExponent;
    }

    /**
     *  Returns the reference distance of this position.
     * @return The reference distance of this position.
     */
    
    public double getReferenceDistance() {
        return referenceDistance;
    }

    /**
     *  Returns the mean path loss at the reference distance of this position.
     * @return The mean path loss at the reference distance of this position.
     */

    public double getReferencePathLoss() {
        return referencePathLoss;
    }

    /**
     *  Returns the shadow fading of this position.
     * @return The shadow fading of this position.
     */
    
    public double getShadowFading() {
        return shadowFading;
    }

    /**
     * Returns the color.
     * @return the color.
     */
    
    public Color getColor() {
        return color;
    }
}
