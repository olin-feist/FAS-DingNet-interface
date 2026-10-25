package HTTP;

import IotDomain.Mote;
import SelfAdaptation.Instrumentation.MoteEffector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Scanner;

/**
 * This class implements the handler for an HTTP request for execute of DingNet.
 * @version 1.0
 */
public class ExecuteHandler implements HttpHandler {

    private static final HTTPResponse OUT_OF_BOUNDS_ADAPTATION = new HTTPResponse(
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Adaptation value out of range.\n"
    );
    /**
     * The simulation state containing information regarding the simulation.
     * @since 1.0
     */
    private final SimulationState simulationState;

    /**
     * An object mapper to map objects to a JSON string.
     * @since 1.0
     */
    private final ObjectMapper objectMapper;

    /**
     * An HTTP Response message {@code INVALID_INPUT} for an invalid JSON format.
     */
    private static final HTTPResponse INVALID_INPUT = new HTTPResponse(
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Malformed input.\n"
    );

    /**
     * An HTTP Response message {@code INVALID_MOTE_ID} for an invalid mote id.
     */
    private static final HTTPResponse INVALID_MOTE_ID = new HTTPResponse(
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Invalid mote ID.\n"
    );

    /**
     * An HTTP Response message {@code INVALID_ADAPTATION} for an invalid adaptation name.
     */
    private static final HTTPResponse INVALID_ADAPTATION = new HTTPResponse(
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Invalid adaptation name.\n"
    );

    /**
     * An HTTP Response message {@code NOT_RUNNING} for when the server is not running.
     */
    private static final HTTPResponse NOT_RUNNING = new HTTPResponse(
            HttpURLConnection.HTTP_CONFLICT,
            "Not running.\n"
    );

    /**
     * Constructs an empty {@code ExecuteHandler} object.
     * @since 1.0
     */
    public ExecuteHandler(SimulationState simulationState) {
        this.simulationState = simulationState;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Executes the adaptation for a given mote with the given value.
     * @param mote The mote.
     * @param adaptationName The field to be adapted.
     * @param value The value to adapt the field with.
     * @throws AdaptationNotFoundException can occur when the given adaptationName is not known.
     */
    private void applyAdaptation(Mote mote, String adaptationName, Double value) throws AdaptationNotFoundException {
        MoteEffector moteEffector = new MoteEffector();

        switch (adaptationName) {
            case "power":
                moteEffector.setPower(mote, value.intValue());
                break;
            case "spreading_factor":
                moteEffector.setSpreadingFactor(mote, value.intValue());
                break;
            case "movement_speed":
                moteEffector.setMovementSpeed(mote, value);
                break;
            case "energy_level":
                moteEffector.setEnergyLevel(mote, value.intValue());
                break;
            default:
                throw new AdaptationNotFoundException();
        }
    }

    /**
     * Sends an HTTP reject response message if applicable.
     * When no HTTP reject response messages have been sent, sends a response with the HTTP Status Code {@code 200 OK}
     * and applies the adaptations to the motes. Overrides the method {@link HttpHandler#handle(HttpExchange)}.
     * @param exchange The HTTP exchange with the execute endpoint.
     * @exception IOException can occur in {@link HttpExchange#sendResponseHeaders(int, long)
     * @since 1.0
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Scanner scanner = new Scanner(exchange.getRequestBody());
        StringBuilder stringBuilder = new StringBuilder();
        List<ExecuteModel> inputs;
        String responseString = "";
        while (scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
        }
        String jsonString = stringBuilder.toString();

        exchange.getResponseHeaders().add("Content-Type", "application/json");

        try {
            inputs = objectMapper.readValue(jsonString, ExecuteDTO.class).getItems();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            INVALID_INPUT.send(exchange);
            return;
        }

        if (!this.simulationState.getIsRunning()) {
            NOT_RUNNING.send(exchange);
            return;
        }

        List<Mote> motes = this.simulationState.getEnvironment().getMotes();

        for (ExecuteModel input : inputs) {
            if (input.getId() < 0 || input.getId() >= motes.size()) {
                INVALID_MOTE_ID.send(exchange);
                return;
            }
        }

        for (ExecuteModel input : inputs) {
            Mote mote = motes.get(input.getId());
            for (AdaptationModel adaptation : input.getAdaptations()) {
                Double value = adaptation.getValue();
                // Retrieve the adaptation option model by name to check its range
                AdaptationOptionModel option = getAdaptationOptionByName(adaptation.getName());
                Double minValue = option != null ? option.getMinValue() : null; // get min
                Double maxValue = option != null ? option.getMaxValue() : null; // get max

                // Check if the adaptation value is within the defined range
                if ((minValue == null || value >= minValue) && (maxValue == null || value <= maxValue)) {
                    try {
                        // Apply the adaptation if the value is within range
                        this.applyAdaptation(mote, adaptation.getName(), value);
                    } catch (AdaptationNotFoundException e) {
                        INVALID_ADAPTATION.send(exchange); // Send error response if adaptation is not found
                        return;
                    }
                    // Update the response string to reflect the applied adaptation
                    responseString = responseString.concat(String.format("Set %s of mote %d to %f.\n",
                            adaptation.getName(), input.getId(), value));
                } else {
                    // Handle out-of-range value
                    System.out.println("Adaptation value out of range for " + adaptation.getName() + ": " + value);
                    OUT_OF_BOUNDS_ADAPTATION.send(exchange);  // New HTTP response for out-of-bounds value
                    return;
                }
            }
        }



        HTTPResponse response = new HTTPResponse(HttpURLConnection.HTTP_OK, responseString);

        response.send(exchange);
    }

    /**
     * An exception that occurs when the given adaptation name is not known.
     */
    private static class AdaptationNotFoundException extends Exception {}

    private AdaptationOptionModel getAdaptationOptionByName(String name) {
        List<AdaptationOptionModel> options = AdaptationOptionsHandler.getAdaptationOptions();
        for (AdaptationOptionModel option : options) {
            if (option.getName().equals(name)) {
                return option;
            }
        }
        return null;
    }

}
