package egovframework.rte.itl.integration.support;

import static org.junit.Assert.assertEquals;

import egovframework.rte.itl.integration.EgovIntegrationMessage;
import egovframework.rte.itl.integration.EgovIntegrationMessageHeader.ResultCode;
import egovframework.rte.itl.integration.EgovIntegrationService;
import egovframework.rte.itl.integration.EgovIntegrationServiceResponse;
import egovframework.rte.itl.integration.message.simple.SimpleMessage;
import egovframework.rte.itl.integration.message.simple.SimpleMessageHeader;

import org.junit.Test;

public class AbstractServiceTest
{

    private static class EchoService extends AbstractService
    {
        private final long serviceTime;
        
        public EchoService(String id, long defaultTimeout, long serviceTime)
        {
            super(id, defaultTimeout);
            this.serviceTime = serviceTime;
        }

        protected EgovIntegrationMessage doSend(
                EgovIntegrationMessage requestMessage)
        {
            try {
                Thread.sleep(serviceTime);
            }
            catch (InterruptedException e)
            {
            }
            return requestMessage;
        }

        public EgovIntegrationMessage createRequestMessage()
        {
            return new SimpleMessage(new SimpleMessageHeader());
        }
    }

    private static final long DEFAULT_TIMEOUT = 5000;
    
    private static final long SERVICE_TIME = DEFAULT_TIMEOUT / 2;
    
    private static final long TIMEOUT_WILL_SUCCEED = SERVICE_TIME * 2;
    
    private static final long TIMEOUT_WILL_BE_TIMED_OUT = SERVICE_TIME / 2;

    private static EgovIntegrationService echoService =
        new EchoService("testService", DEFAULT_TIMEOUT, SERVICE_TIME);
    
    private static final EgovIntegrationMessage requestMessage =
        echoService.createRequestMessage();
    
    @Test
    public void testSendSyncSucceeds() throws Exception
    {
        EgovIntegrationMessage responseMessage =
            echoService.sendSync(requestMessage);
        
        assertEquals(requestMessage, responseMessage);
    }

    @Test
    public void testSendSyncWithTimeoutSucceeds() throws Exception
    {
        EgovIntegrationMessage responseMessage =
            echoService.sendSync(requestMessage, TIMEOUT_WILL_SUCCEED);
        
        assertEquals(requestMessage, responseMessage);
    }
    
    @Test
    public void testSendSyncTimedOut() throws Exception
    {
        EgovIntegrationMessage responseMessage =
            echoService.sendSync(requestMessage, TIMEOUT_WILL_BE_TIMED_OUT);
        assertEquals(ResultCode.TIME_OUT,
                responseMessage.getHeader().getResultCode());
    }
    
    @Test
    public void testSendAsyncSucceeds() throws Exception
    {
        EgovIntegrationServiceResponse response =
            echoService.sendAsync(requestMessage);
        
        assertEquals(requestMessage, response.receive());
    }
    
    @Test
    public void testSendAsyncWithTimeoutSucceeds() throws Exception
    {
        EgovIntegrationServiceResponse response =
            echoService.sendAsync(requestMessage);
        
        assertEquals(requestMessage, response.receive(TIMEOUT_WILL_SUCCEED));
    }
    
    @Test
    public void testSendAsyncTimedOut() throws Exception
    {
        EgovIntegrationServiceResponse response =
            echoService.sendAsync(requestMessage);
        
        EgovIntegrationMessage responseMessage =
            response.receive(TIMEOUT_WILL_BE_TIMED_OUT);
        assertEquals(ResultCode.TIME_OUT,
                responseMessage.getHeader().getResultCode());
    }
}
