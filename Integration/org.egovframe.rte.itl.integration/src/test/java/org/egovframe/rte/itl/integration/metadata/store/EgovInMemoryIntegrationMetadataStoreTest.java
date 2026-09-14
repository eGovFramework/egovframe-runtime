package org.egovframe.rte.itl.integration.metadata.store;

import org.egovframe.rte.itl.integration.metadata.IntegrationDefinition;
import org.egovframe.rte.itl.integration.metadata.OrganizationDefinition;
import org.egovframe.rte.itl.integration.metadata.ServiceDefinition;
import org.egovframe.rte.itl.integration.metadata.SystemDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovInMemoryIntegrationMetadataStore 의 DAO 5종 계약과 스냅숏 교체를 검증한다.
 */
public class EgovInMemoryIntegrationMetadataStoreTest {

    private EgovInMemoryIntegrationMetadataStore store;
    private OrganizationDefinition providerOrganization;
    private SystemDefinition providerSystem;
    private ServiceDefinition providerService;
    private SystemDefinition consumerSystem;
    private IntegrationDefinition integration;

    @BeforeEach
    public void setUpGraph() {
        providerOrganization = new OrganizationDefinition("ORG1", "행정안전부");
        providerSystem = new SystemDefinition("SYS-KEY-1", providerOrganization, "SYS1", "인사시스템", true);
        providerOrganization.getSystems().put(providerSystem.getId(), providerSystem);
        providerService = new ServiceDefinition("SVC-KEY-1", providerSystem, "SVC1", "직원조회",
                "reqType", "resType", "employeeServiceProvider", true, true);
        providerSystem.getServices().put(providerService.getId(), providerService);

        OrganizationDefinition consumerOrganization = new OrganizationDefinition("ORG2", "국세청");
        consumerSystem = new SystemDefinition("SYS-KEY-2", consumerOrganization, "SYS2", "세정시스템", false);
        consumerOrganization.getSystems().put(consumerSystem.getId(), consumerSystem);

        integration = new IntegrationDefinition("INT-001", providerService, consumerSystem, 5000, true, null, null);

        store = new EgovInMemoryIntegrationMetadataStore();
        store.reload(List.of(providerOrganization, consumerOrganization), List.of(integration), null);
    }

    @Test
    public void testOrganizationSystemAndServiceLookupsByKeyAndAlternativeKeys() {
        assertSame(providerOrganization, store.getOrganizationDefinition("ORG1"));
        assertSame(providerSystem, store.getSystemDefinition("SYS-KEY-1"));
        assertSame(providerSystem, store.getSystemDefinition("ORG1", "SYS1"));
        assertSame(providerService, store.getServiceDefinition("SVC-KEY-1"));
        assertSame(providerService, store.getServiceDefinition("SYS-KEY-1", "SVC1"));
        assertSame(providerService, store.getServiceDefinition("ORG1", "SYS1", "SVC1"));
        assertNull(store.getOrganizationDefinition("ORG-X"));
        assertNull(store.getServiceDefinition("ORG1", "SYS1", "SVC-X"));
    }

    @Test
    public void testIntegrationLookupAndConsumerProviderFilters() {
        assertSame(integration, store.getIntegrationDefinition("INT-001"));

        List<IntegrationDefinition> ofConsumer = store.getIntegrationDefinitionOfConsumer("ORG2", "SYS2");
        assertEquals(1, ofConsumer.size());
        assertSame(integration, ofConsumer.get(0));
        assertTrue(store.getIntegrationDefinitionOfConsumer("ORG1", "SYS1").isEmpty());

        List<IntegrationDefinition> ofProvider = store.getIntegrationDefinitionOfProvider("ORG1", "SYS1");
        assertEquals(1, ofProvider.size());
        assertSame(integration, ofProvider.get(0));
        assertTrue(store.getIntegrationDefinitionOfProvider("ORG2", "SYS2").isEmpty());
    }

    @Test
    public void testReloadReplacesTheSnapshotAtomically() {
        store.reload(List.of(), List.of(), null);

        assertNull(store.getOrganizationDefinition("ORG1"));
        assertNull(store.getIntegrationDefinition("INT-001"));
        assertTrue(store.getIntegrationDefinitionOfConsumer("ORG2", "SYS2").isEmpty());
        assertNull(store.getRecordTypeDefinition("anyType"));
    }

}
