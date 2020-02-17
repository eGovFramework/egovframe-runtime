package egovframework.rte.itl.webservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Calendar;

import egovframework.rte.itl.integration.EgovIntegrationMessage;
import egovframework.rte.itl.integration.EgovIntegrationMessageHeader;
import egovframework.rte.itl.integration.EgovIntegrationMessageHeader.ResultCode;
import egovframework.rte.itl.integration.metadata.IntegrationDefinition;
import egovframework.rte.itl.integration.metadata.OrganizationDefinition;
import egovframework.rte.itl.integration.metadata.ServiceDefinition;
import egovframework.rte.itl.integration.metadata.SystemDefinition;
import egovframework.rte.itl.webservice.service.EgovWebServiceClient;

import org.junit.Test;

public class EgovWebServiceTest
{
    private final OrganizationDefinition providerOrganization =
        new OrganizationDefinition("org0", "provider organization");
    
    private final SystemDefinition providerSystem =
        new SystemDefinition("sys0", providerOrganization, "sys0", "provider system", true);

    private final ServiceDefinition providerService =
        new ServiceDefinition("srv0", providerSystem, "srv0", "provider service", "req", "res", "providerBean", true, true);

    private final OrganizationDefinition consumerOrganization =
        new OrganizationDefinition("org1", "consumer organization");
    
    private final SystemDefinition consumerSystem =
        new SystemDefinition("sys1", consumerOrganization, "sys0", "consumer system", true);

    private final IntegrationDefinition integrationDefinition =
        new IntegrationDefinition("test", providerService, consumerSystem, 5000, true, null, null);
    
    private final EgovWebServiceClient echoClient = new EchoEgovWebServiceClient(0, ResultCode.OK);

    @Test
    public void testCreationSucceeds() throws Exception
    {
        try
        {
            new EgovWebService("test", 5000, integrationDefinition, echoClient);
        }
        catch (Throwable e)
        {
            fail();
        }
    }
    
    @Test
    public void testCreationFailsWithIllegalArgument() throws Exception
    {
        // id
        try
        {
            new EgovWebService("  ", 5000, integrationDefinition, echoClient);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
        
        // integrationDefinition
        try
        {
            new EgovWebService("test", 5000, null, echoClient);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }

        // integrationDefinition.provider
        try
        {
            new EgovWebService("test", 5000,
                    new IntegrationDefinition("test", null, consumerSystem, 5000, true, null, null),
                    echoClient);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
        
        // integrationDefinition.provider.system
        try
        {
            new EgovWebService("test", 5000,
                    new IntegrationDefinition("test",
                            new ServiceDefinition("test", null, "test", "test", "req", "rse", null, false, false),
                            consumerSystem, 5000, true, null, null),
                    echoClient);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
        
        // integrationDefinition.provider.system.organization
        try
        {
            new EgovWebService("test", 5000,
                    new IntegrationDefinition("test",
                            new ServiceDefinition("test",
                                    new SystemDefinition("test", null, "test", "test", true),
                                    "test", "test", "req", "rse", null, false, false),
                            consumerSystem, 5000, true, null, null),
                    echoClient);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
        
        // integrationDefinition.consumer
        try
        {
            new EgovWebService("test", 5000,
                    new IntegrationDefinition("test", providerService, null, 5000, true, null, null),
                    echoClient);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }

        // integrationDefinition.consumer.organization
        try
        {
            new EgovWebService("test", 5000,
                    new IntegrationDefinition("test", providerService,
                            new SystemDefinition("test", null, "test", "test", true),
                            5000, true, null, null),
                    echoClient);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
        
        // client
        try
        {
            new EgovWebService("test", 5000, integrationDefinition, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }
    
    @Test
    public void testCreateRequestMessageSucceeds() throws Exception
    {
        EgovWebService service =
            new EgovWebService("test", 5000, integrationDefinition, echoClient);
        
        EgovIntegrationMessage requestMessage = service.createRequestMessage();
        
        assertNotNull(requestMessage);
        assertNotNull(requestMessage.getHeader());
        EgovIntegrationMessageHeader header = requestMessage.getHeader();
        assertEquals(integrationDefinition.getId(), header.getIntegrationId());
        assertEquals(providerOrganization.getId(), header.getProviderOrganizationId());
        assertEquals(providerSystem.getId(), header.getProviderSystemId());
        assertEquals(providerService.getId(), header.getProviderServiceId());
        assertEquals(consumerOrganization.getId(), header.getConsumerOrganizationId());
        assertEquals(consumerSystem.getId(), header.getConsumerSystemId());
        assertNotNull(requestMessage.getBody());
        assertEquals(0, requestMessage.getBody().size());
    }
    
    @Test
    public void testDoSendSucceeds() throws Exception
    {
        EgovWebService service =
            new EgovWebService("test", 5000, integrationDefinition, echoClient);
        
        EgovIntegrationMessage requestMessage = service.createRequestMessage();
        EgovIntegrationMessage responseMessage = service.doSend(requestMessage);
        
        assertNotNull(responseMessage);
        assertNotNull(responseMessage.getHeader());
        EgovIntegrationMessageHeader requestHeader = requestMessage.getHeader();
        EgovIntegrationMessageHeader responseHeader = responseMessage.getHeader();
        assertEquals(requestHeader.getIntegrationId(), responseHeader.getIntegrationId());
        assertEquals(requestHeader.getProviderOrganizationId(), responseHeader.getProviderOrganizationId());
        assertEquals(requestHeader.getProviderSystemId(), responseHeader.getProviderSystemId());
        assertEquals(requestHeader.getProviderServiceId(), responseHeader.getProviderServiceId());
        assertEquals(requestHeader.getConsumerOrganizationId(), responseHeader.getConsumerOrganizationId());
        assertEquals(requestHeader.getConsumerSystemId(), responseHeader.getConsumerSystemId());
        assertEquals(requestHeader.getRequestSendTime(), responseHeader.getRequestSendTime());
        assertNotNull(responseHeader.getRequestReceiveTime());
        assertNotNull(responseHeader.getResponseSendTime());
        assertNotNull(responseHeader.getResponseReceiveTime());
        assertEquals(ResultCode.OK, responseHeader.getResultCode());
    }
    
    @Test
    public void testDoSendFailsWithIntegrationIsNotUsable() throws Exception
    {
        EgovWebService service =
            new EgovWebService("test", 5000,
                    new IntegrationDefinition("test", providerService, consumerSystem, 5000, false, null, null),
                    echoClient);
        
        EgovIntegrationMessage requestMessage = service.createRequestMessage();
        EgovIntegrationMessage responseMessage = service.doSend(requestMessage);
        
        assertNotNull(responseMessage);
        assertNotNull(responseMessage.getHeader());
        assertEquals(ResultCode.NOT_USABLE_INTEGRATION, responseMessage.getHeader().getResultCode());
    }
    
    @Test
    public void testDoSendFailsWithInvalidTime() throws Exception
    {
        IntegrationDefinition integrationDefinition = new IntegrationDefinition("test", providerService, consumerSystem, 5000, true, null, null);
        
        EgovWebService service =
            new EgovWebService("test", 5000, integrationDefinition, echoClient);
        
        Calendar from = Calendar.getInstance();
        from.add(Calendar.YEAR, 1);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.YEAR, -1);
        
        // from
        integrationDefinition.setValidateFrom(from);
        integrationDefinition.setValidateTo(null);
        
        EgovIntegrationMessage requestMessage = service.createRequestMessage();
        EgovIntegrationMessage responseMessage = service.doSend(requestMessage);
        
        assertNotNull(responseMessage);
        assertNotNull(responseMessage.getHeader());
        assertEquals(ResultCode.INVALID_TIME, responseMessage.getHeader().getResultCode());
        
        // to
        integrationDefinition.setValidateFrom(null);
        integrationDefinition.setValidateTo(to);
        
        requestMessage = service.createRequestMessage();
        responseMessage = service.doSend(requestMessage);
        
        assertNotNull(responseMessage);
        assertNotNull(responseMessage.getHeader());
        assertEquals(ResultCode.INVALID_TIME, responseMessage.getHeader().getResultCode());
    }
    
    @Test
    public void testDoSendFailsWithServiceIsNotUsable() throws Exception
    {
        EgovWebService service =
            new EgovWebService("test", 5000,
                    new IntegrationDefinition("test",
                            new ServiceDefinition("srv", providerSystem, "srv", "srv", "req", "res", null, true, false),
                            consumerSystem, 5000, true, null, null),
                    echoClient);
        
        EgovIntegrationMessage requestMessage = service.createRequestMessage();
        EgovIntegrationMessage responseMessage = service.doSend(requestMessage);
        
        assertNotNull(responseMessage);
        assertNotNull(responseMessage.getHeader());
        assertEquals(ResultCode.NOT_USABLE_SERVICE, responseMessage.getHeader().getResultCode());
    }
}

class EchoEgovWebServiceClient implements EgovWebServiceClient
{
    private long waitTime;
    
    private ResultCode resultCode;
    
    public EchoEgovWebServiceClient(long waitTime, ResultCode resultCode)
    {
        super();
        this.waitTime = waitTime;
        this.resultCode = resultCode;
    }

    public EgovIntegrationMessage service(
            EgovIntegrationMessage requestMessage)
    {
        EgovWebServiceMessageHeader responseHeader =
            new EgovWebServiceMessageHeader(requestMessage.getHeader());
        responseHeader.setRequestReceiveTime(Calendar.getInstance());
        
        try
        {
            Thread.sleep(waitTime);
        }
        catch (InterruptedException e)
        {
        }
        
        responseHeader.setResponseSendTime(Calendar.getInstance());
        responseHeader.setResultCode(resultCode);
        return new EgovWebServiceMessage(responseHeader);
    }
};
