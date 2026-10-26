package HTTP;

import Simulation.MainSimulation;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.SimulationState;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Random;

/**
 * This class implements the handler for starting a run in DingNet.
 * @version 1.0
 */
public class StartRunHandler implements HttpHandler {
    /**
     * The simulation state containing information regarding the simulation.
     * @since 1.0
     */
    private final SimulationState simulationState;

    /**
     * An HTTP Response message {@code ALREADY_RUNNING} for when DingNet is already running.
     */
    static private final HTTPResponse ALREADY_RUNNING = new HTTPResponse(
            HttpURLConnection.HTTP_CONFLICT,
            "Simulation is already running.\n"
    );

    /**
     * An HTTP Response message {@code INVALID_SEED} for when the seed query parameter is not a whole number.
     */
    static private final HTTPResponse INVALID_SEED = new HTTPResponse(
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Invalid seed.\n"
    );

    /**
     * Constructs an {@code StartRunHandler} object with the simulation state {@code simulationState}.
     * @param simulationState The state of the simulation to be started.
     * @since 1.0
     */
    public StartRunHandler(SimulationState simulationState) {
        this.simulationState = simulationState;
    }

    /**
     * Starts the simulation of DingNet (if it wasn't running already).
     * The function also resets the SimulationState.
     * The optional query parameter seed seeds the random number generators,
     * so runs with the same seed get the same random values. The response
     * reports the seed used, so the run can be repeated.
     * @param exchange the exchange containing the request from the
     *      client and used to send the response
     * @exception IOException can occur when sending the HTTP response
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (this.simulationState.getIsRunning()) {
            ALREADY_RUNNING.send(exchange);
            return;
        }

        long seed;
        try {
            seed = getSeed(exchange.getRequestURI().getRawQuery());
        } catch (NumberFormatException e) {
            INVALID_SEED.send(exchange);
            return;
        }

        this.simulationState.setIsRunning(true);
        this.simulationState.setShouldStop(false);
        new MainSimulation(this.simulationState, seed).start();

        new HTTPResponse(HttpURLConnection.HTTP_OK, "Simulation started with seed " + seed + ".\n").send(exchange);
    }

    /**
     * Returns the value of the seed, or a random seed if it is absent.
     * @param rawQuery the query string of the request
     * @return the seed.
     * @exception NumberFormatException if the seed is not a whole number.
     */
    private static long getSeed(String rawQuery) {
        if (rawQuery != null) {
            for (String parameter : rawQuery.split("&")) {
                String[] keyValue = parameter.split("=", 2);
                if (keyValue.length == 2 && keyValue[0].equals("seed")) {
                    return Long.parseLong(keyValue[1]);
                }
            }
        }
        return new Random().nextLong();
    }
}
