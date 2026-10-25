package HTTP;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.AdaptationOptionModel;
import models.AdaptationOptionsDTO;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

/**
 * This class implements the handler for an HTTP request for the adaption options of DingNet.
 * @version 1.0
 */
public class AdaptationOptionsHandler implements HttpHandler {

    /**
     * The models of adaptation options to be described by the HTTP response.
     * @since 1.0
     */
    public static final List<AdaptationOptionModel> ADAPTATION_OPTIONS = Arrays.asList(
        new AdaptationOptionModel(
                "power",
                "Determines the energy consumed by the mote for communication.",
                -1D,
                15D
        ),
        new AdaptationOptionModel(
                "spreading_factor",
                "Determines the duration of the communication",
                7D,
                12D
        ),
        new AdaptationOptionModel(
                "movement_speed",
                "The movement speed of the mote.",
                0D,
                null
        ),
        new AdaptationOptionModel(
                "energy_level",
                "The energy level of the mote.",
                0D,
                null
        )
    );

    public static List<AdaptationOptionModel> getAdaptationOptions() {
        return ADAPTATION_OPTIONS;
    }

    /**
     * An object mapper to map objects to a JSON string.
     * @since 1.0
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructs an empty {@code AdaptationOptionsHandler} object.
     * @since 1.0
     */
    public AdaptationOptionsHandler() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends a response with the HTTP Status Code {@code 200 OK} and the adaption options of DingNet as JSON
     * to the HTTP {@code exchange}. Overrides the method {@link HttpHandler#handle(HttpExchange)}.
     * @param exchange The HTTP exchange with the adaptation options endpoint.
     * @exception IOException can occur in {@link HttpExchange#sendResponseHeaders(int, long)
     * @since 1.0
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String data = this.objectMapper.writeValueAsString(new AdaptationOptionsDTO(ADAPTATION_OPTIONS));
        HTTPResponse response = new HTTPResponse (HttpURLConnection.HTTP_OK, data);

        exchange.getResponseHeaders().add("Content-Type", "application/json");

        response.send(exchange);
    }
}
