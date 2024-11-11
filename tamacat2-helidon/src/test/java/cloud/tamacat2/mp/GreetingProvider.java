package cloud.tamacat2.mp;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * https://helidon.io/docs/v4/mp/guides/mp-tutorial
 */
@ApplicationScoped
public class GreetingProvider {
    private final AtomicReference<String> message = new AtomicReference<>(); 

    @Inject
    public GreetingProvider(@ConfigProperty(name = "app.greeting") String message) {
        this.message.set(message);
    }

    String getMessage() {
        return message.get();
    }

    void setMessage(String message) {
        this.message.set(message);
    }
}
