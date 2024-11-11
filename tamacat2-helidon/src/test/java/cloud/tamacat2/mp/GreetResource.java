package cloud.tamacat2.mp;

import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * https://helidon.io/docs/v4/mp/guides/mp-tutorial
 */
@Path("/greet")
@RequestScoped
public class GreetResource {
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Map.of());
    private final GreetingProvider greetingProvider;

    @Inject
    public GreetResource(GreetingProvider greetingConfig) {
        this.greetingProvider = greetingConfig;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getDefaultMessage() {
        return createResponse("World"); 
    }

    private JsonObject createResponse(String who) { 
        String msg = String.format("%s %s!", greetingProvider.getMessage(), who);
        return JSON.createObjectBuilder()
                .add("message", msg)
                .build();
    }
}
