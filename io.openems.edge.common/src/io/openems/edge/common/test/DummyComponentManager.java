package io.openems.edge.common.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.openems.common.types.EdgeConfig;
import io.openems.edge.common.access_control.Role;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import org.slf4j.Logger;

/**
 * Simulates a ComponentManager for the OpenEMS Component test framework.
 */
public class DummyComponentManager implements ComponentManager {

    private final List<OpenemsComponent> components = new ArrayList<>();

    public DummyComponentManager() {
    }

    @Override
    public List<OpenemsComponent> getComponents(Role role) {
        return Collections.unmodifiableList(this.components);
    }

    @Override
    public List<OpenemsComponent> getComponents() {
        return this.getComponents(null);
    }

    /**
     * Specific for this Dummy implementation.
     *
     * @param component
     */
    public void addComponent(OpenemsComponent component) {
        if (component != this) {
            this.components.add(component);
        }
    }

    @Override
    public EdgeConfig getEdgeConfig(Role role) {
        return new EdgeConfig();
    }

    @Override
    public EdgeConfig getEdgeConfig() {
        return new EdgeConfig();
    }

    @Override
    public boolean isComponentActivated(String componentId, String pid) {
        return false;
        // TODO take care of this
    }

    @Override
    public void logWarn(Logger log, String s) {
    // TODO take care of this
    }

    @Override
    public void logError(Logger log, String message) {
    // TODO take care of this
    }

    @Override
    public List<String> checkForNotActivatedComponents() {
        return null;
        // TODO take care of this
    }

}