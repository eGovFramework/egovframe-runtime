package org.egovframe.rte.itl.webservice.service.impl;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebParam.Mode;
import jakarta.jws.WebService;
import jakarta.xml.ws.Holder;
import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessageHeader;
import org.egovframe.rte.itl.webservice.service.ServiceBridge;
import org.egovframe.rte.itl.webservice.service.ServiceEndpointInfo;
import org.egovframe.rte.itl.webservice.service.ServiceParamInfo;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class EgovWebServiceClassLoaderImplTest {

    private final EgovWebServiceClassLoaderImpl classLoader = new EgovWebServiceClassLoaderImpl(getClass().getClassLoader());

    /**
     * ServiceEndpointInfoImpl(WebServiceServerDefinition, RecordType, RecordType)이 만드는 구성과 같다.
     * header는 INOUT, request field는 IN, response field는 OUT이다.
     */
    private final ServiceEndpointInfo serviceEndpointInfo = new ServiceEndpointInfoImpl(
            "http://test/", "/A/test", "ServiceA", "PortA", "service", null,
            new ArrayList<ServiceParamInfo>() {
                /**
                 *  serialVersion UID
                 */
                private static final long serialVersionUID = -6098622390136248349L;

                {
                    add(new ServiceParamInfoImpl("header", EgovWebServiceMessageHeader.TYPE, Mode.INOUT, true));
                    add(new ServiceParamInfoImpl("request", PrimitiveType.STRING, Mode.IN, false));
                    add(new ServiceParamInfoImpl("response", PrimitiveType.STRING, Mode.OUT, false));
                }
            });

    private Method getOperation(Class<?> serviceEndpointInterfaceClass) throws Exception {
        return serviceEndpointInterfaceClass.getDeclaredMethod("service", Holder.class, String.class, Holder.class);
    }

    @Test
    public void testServiceEndpointInterfaceHasWebServiceAnnotations() throws Exception {
        Class<?> serviceEndpointInterfaceClass = classLoader.loadClass(serviceEndpointInfo).getInterfaces()[0];
        Method operation = getOperation(serviceEndpointInterfaceClass);

        assertNotNull(serviceEndpointInterfaceClass.getAnnotation(WebService.class));
        assertNotNull(operation.getAnnotation(WebMethod.class));

        Annotation[][] parameterAnnotations = operation.getParameterAnnotations();
        assertEquals(3, parameterAnnotations.length);
        for (Annotation[] annotations : parameterAnnotations) {
            assertEquals(1, annotations.length);
            assertInstanceOf(WebParam.class, annotations[0]);
        }
    }

    @Test
    public void testHolderParameterSignatureMatchesDescriptor() throws Exception {
        Class<?> serviceEndpointInterfaceClass = classLoader.loadClass(serviceEndpointInfo).getInterfaces()[0];
        Method operation = getOperation(serviceEndpointInterfaceClass);

        // Signature 속성이 method descriptor와 다른 Holder를 가리키면 TypeNotPresentException이 발생한다.
        java.lang.reflect.Type[] genericParameterTypes = operation.getGenericParameterTypes();

        assertInstanceOf(ParameterizedType.class, genericParameterTypes[0]);
        assertEquals(Holder.class, ((ParameterizedType) genericParameterTypes[0]).getRawType());
        assertEquals(EgovWebServiceMessageHeader.class, ((ParameterizedType) genericParameterTypes[0]).getActualTypeArguments()[0]);

        assertInstanceOf(ParameterizedType.class, genericParameterTypes[2]);
        assertEquals(Holder.class, ((ParameterizedType) genericParameterTypes[2]).getRawType());
        assertEquals(String.class, ((ParameterizedType) genericParameterTypes[2]).getActualTypeArguments()[0]);
    }

    @Test
    public void testServiceEndpointHasServiceBridgeField() throws Exception {
        Class<?> serviceEndpointClass = classLoader.loadClass(serviceEndpointInfo);

        // field 타입이 존재하지 않는 class이면 getField()에서 NoClassDefFoundError가 발생한다.
        assertEquals(ServiceBridge.class, serviceEndpointClass.getField(classLoader.getFieldNameOfServiceBridge()).getType());
    }

}
