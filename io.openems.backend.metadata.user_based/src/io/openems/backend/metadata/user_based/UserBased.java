package io.openems.backend.metadata.user_based;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.openems.backend.common.component.AbstractOpenemsBackendComponent;
import io.openems.common.channel.Level;
import io.openems.common.utils.FileUtils;
import io.openems.backend.metadata.api.BackendUser;
import io.openems.backend.metadata.api.Edge;
import io.openems.backend.metadata.api.Edge.State;
import io.openems.backend.metadata.api.Metadata;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.ChannelAddress;
import io.openems.common.types.EdgeConfig;
import io.openems.common.utils.JsonKeys;
import io.openems.common.utils.JsonUtils;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO Fill the comment as soon as the logic works
 */
@Designate(ocd = Config.class, factory = false)
@Component(name = "Metadata.UserBased", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class UserBased extends AbstractOpenemsBackendComponent implements Metadata {

    private final Logger log = LoggerFactory.getLogger(UserBased.class);
    private final Map<String, BackendUser> sessionIdUserMap = new HashMap<>();
    private final Map<String, BackendUser> userMap = new HashMap<>();
    private final Map<BackendUser, String> userPasswordMapping = new HashMap<>();
    private final Map<String, Edge> edges = new HashMap<>();
    private String path = "";
    private AtomicInteger sessionId = new AtomicInteger(0);

    public UserBased() {
        super("Metadata.UserBased");
    }

    @Activate
    void activate(Config config) {
        log.info("Activate [path=" + config.path() + "]");
        this.path = config.path();

        // Read the data async
        CompletableFuture.runAsync(this::refreshData);
    }

    @Deactivate
    void deactivate() {
        this.logInfo(this.log, "Deactivate");
    }

    @Override
    public synchronized Optional<String> getEdgeIdForApikey(String apikey) {
        Optional<Optional<Entry<String, Edge>>> edgeId = Optional.of(this.edges.entrySet().stream().filter(
                e -> e.getValue().getApikey().equals(apikey)).findFirst());
        if (edgeId.isPresent() && edgeId.get().isPresent()) {
            return Optional.of(edgeId.get().get().getValue().getId());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public synchronized Optional<Edge> getEdge(String edgeId) {
        this.refreshData();
        Edge edge = this.edges.get(edgeId);
        return Optional.ofNullable(edge);
    }

    /**
     * In case there is a path for a configuration file configured, this method extracts the JSON-encoded information
     * and fills the fields of the class.<br>
     * See also: {@link UserBased#userMap}, {@link UserBased#edges}
     */
    private synchronized void refreshData() {
        if (!this.edges.isEmpty()) {
            return;
        }

        StringBuilder sb = FileUtils.checkAndGetFileContent(this.path);
        if (sb == null) {
            // exception occurred. File could not be read
            return;
        }

        List<Edge> edges = new ArrayList<>();

        // parse to JSON
        try {
            JsonElement config = JsonUtils.parse(sb.toString());
            JsonArray jUsers = JsonUtils.getAsJsonArray(config, JsonKeys.USERS.value());
            for (JsonElement jUser : jUsers) {
                // handle the user
                String userId = JsonUtils.getAsString(jUser, JsonKeys.USER_ID.value());
                String name = JsonUtils.getAsString(jUser, JsonKeys.NAME.value());
                BackendUser user = new BackendUser(userId, name);
                this.userMap.put(userId, user);
                // handle the connected edges
                for (JsonElement jEdge : JsonUtils.getAsJsonArray(jUser, JsonKeys.EDGES.value())) {
                    String edgeId = JsonUtils.getAsString(jEdge, JsonKeys.EDGE_ID.value());
                    edges.add(new Edge(//
                            edgeId,
                            JsonUtils.getAsString(jEdge, JsonKeys.API_KEY.value()), //
                            JsonUtils.getAsString(jEdge, JsonKeys.COMMENT.value()), //
                            State.ACTIVE, // State
                            "", // Version
                            "", // Product-Type
                            new EdgeConfig(), // Config
                            null, // State of Charge
                            null, // IPv4
                            Level.OK
                    ));
                }
            }
        } catch (OpenemsNamedException e) {
            this.logWarn(this.log, "Unable to parse JSON-file [" + this.path + "]: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Add Edges and configure User permissions
        for (Edge edge : edges) {
            this.edges.put(edge.getId(), edge);
        }
    }
}
