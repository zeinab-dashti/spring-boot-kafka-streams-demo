package space.zeinab.demo.orderService.topology;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
@Scope("singleton")
public class SchemaRegistryConfig {
    public final String url;
    public final String keyStoreLocation;
    public final String keyStorePassword;
    public final String keyPassword;
    public final String trustStoreLocation;
    public final String trustStorePassword;

    @Autowired
    public SchemaRegistryConfig(
            @Value("${spring.kafka.properties.schema.registry.url}") final String url,
            @Value("${spring.kafka.properties.schema.registry.ssl.keystore.location}") final String keyStoreLocation,
            @Value("${spring.kafka.properties.schema.registry.ssl.keystore.password}") final String keyStorePassword,
            @Value("${spring.kafka.properties.schema.registry.ssl.key.password}") final String keyPassword,
            @Value("${spring.kafka.properties.schema.registry.ssl.truststore.location}") final String trustStoreLocation,
            @Value("${spring.kafka.properties.schema.registry.ssl.truststore.password}") final String trustStorePassword) {
        this.url = url;
        this.keyStoreLocation = keyStoreLocation;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
        this.trustStoreLocation = trustStoreLocation;
        this.trustStorePassword = trustStorePassword;
    }

    public Map<String, Object> buildPropertiesMap() {
        return buildPropertiesMap(this);
    }

    public static Map<String, Object> buildPropertiesMap(SchemaRegistryConfig config) {
        Map<String, Object> rv = new HashMap<>();
        Properties props = buildProperties(config);
        for (final String name : props.stringPropertyNames())
            rv.put(name, props.getProperty(name));
        return rv;
    }

    public static Properties buildProperties(SchemaRegistryConfig config) {
        final Properties rv = new Properties();
        rv.put("schema.registry.url", config.url);
        rv.put("schema.registry.ssl.keystore.location", config.keyStoreLocation);
        rv.put("schema.registry.ssl.keystore.password", config.keyStorePassword);
        rv.put("schema.registry.ssl.key.password", config.keyPassword);
        rv.put("schema.registry.ssl.truststore.location", config.trustStoreLocation);
        rv.put("schema.registry.ssl.truststore.password", config.trustStorePassword);
        return rv;
    }

}
