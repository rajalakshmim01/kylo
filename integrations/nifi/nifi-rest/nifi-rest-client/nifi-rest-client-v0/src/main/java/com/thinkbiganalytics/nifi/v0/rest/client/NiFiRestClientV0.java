package com.thinkbiganalytics.nifi.v0.rest.client;

import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiTemplatesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.Entity;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;

/**
 * Implements a {@link NiFiRestClient} for communicating with NiFi v0.6.
 */
public class NiFiRestClientV0 extends JerseyRestClient implements NiFiRestClient {

    /** Parameter name for the revision client ID */
    private static final String CLIENT_ID = "clientId";

    /** Parameter name for the revision version */
    private static final String VERSION = "version";

    /** NiFi REST client configuration */
    @Nonnull
    private final NifiRestClientConfig clientConfig;

    /** NiFi Controller Services REST client */
    @Nullable
    private NiFiControllerServicesRestClientV0 controllerServices;

    /** NiFi Process Groups REST client */
    @Nullable
    private NiFiProcessGroupsRestClientV0 processGroups;

    /** NiFi Templates REST client */
    @Nullable
    private NiFiTemplatesRestClientV0 templates;

    /**
     * Constructs a {@code NiFiRestClientV0} with the specified NiFi REST client configuration.
     *
     * @param config the NiFi REST client configuration
     */
    public NiFiRestClientV0(@Nonnull final NifiRestClientConfig config) {
        super(config);
        clientConfig = config;
    }

    @Nonnull
    @Override
    public NiFiControllerServicesRestClient controllerServices() {
        if (controllerServices == null) {
            controllerServices = new NiFiControllerServicesRestClientV0(this);
        }
        return controllerServices;
    }

    @Nonnull
    @Override
    public <T> T delete(@Nonnull final String path, @Nullable final Map<String, Object> params, @Nonnull final Class<T> returnType) {
        if (params != null) {
            final RevisionDTO revision = getRevision();
            params.put(CLIENT_ID, revision.getClientId());
            params.put(VERSION, revision.getVersion());
        }
        return super.delete(path, params, returnType);
    }

    /**
     * Gets the NiFi cluster type.
     *
     * @return the NiFi cluster type
     */
    public String getClusterType() {
        return clientConfig.getClusterType();
    }

    @Nonnull
    @Override
    public <T> T post(@Nonnull final String path, @Nullable final Object object, @Nonnull final Class<T> returnType) {
        return super.post(path, updateEntityForSave(object), returnType);
    }

    @Nonnull
    @Override
    public <T> T postForm(@Nonnull final String path, @Nullable final Form form, @Nonnull final Class<T> returnType) {
        if (form != null) {
            final RevisionDTO revision = getRevision();
            form.param(CLIENT_ID, revision.getClientId());
            form.param(VERSION, revision.getVersion().toString());
        }
        return super.postForm(path, form, returnType);
    }

    @Nonnull
    @Override
    public <T> T put(@Nonnull final String path, @Nullable final Object object, @Nonnull final Class<T> returnType) {
        return super.put(path, updateEntityForSave(object), returnType);
    }

    @Nonnull
    @Override
    public NiFiProcessGroupsRestClient processGroups() {
        if (processGroups == null) {
            processGroups = new NiFiProcessGroupsRestClientV0(this);
        }
        return processGroups;
    }

    @Nonnull
    @Override
    public NiFiTemplatesRestClient templates() {
        if (templates == null) {
            templates = new NiFiTemplatesRestClientV0(this);
        }
        return templates;
    }

    @Override
    protected WebTarget getBaseTarget() {
        return super.getBaseTarget().path("/nifi-api");
    }

    /**
     * Gets the current Revision and Version of Nifi instance. This is needed when performing an update to pass over the revision.getVersion() for locking purposes
     */
    private RevisionDTO getRevision() {
        return get("/controller/revision", null, Entity.class).getRevision();
    }

    /**
     * Updates the specified REST entity for a {@code POST} or {@code PUT} operation.
     *
     * @param object the entity
     * @return the entity
     */
    private Object updateEntityForSave(@Nullable final Object object) {
        if (object instanceof Entity) {
            ((Entity)object).setRevision(getRevision());
        }
        return object;
    }
}