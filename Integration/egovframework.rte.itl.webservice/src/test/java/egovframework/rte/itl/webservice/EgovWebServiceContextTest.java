package egovframework.rte.itl.webservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import egovframework.rte.itl.integration.EgovIntegrationMessage;
import egovframework.rte.itl.integration.EgovIntegrationServiceProvider;
import egovframework.rte.itl.integration.metadata.IntegrationDefinition;
import egovframework.rte.itl.integration.metadata.OrganizationDefinition;
import egovframework.rte.itl.integration.metadata.ServiceDefinition;
import egovframework.rte.itl.integration.metadata.SystemDefinition;
import egovframework.rte.itl.integration.metadata.dao.IntegrationDefinitionDao;
import egovframework.rte.itl.integration.type.RecordType;
import egovframework.rte.itl.integration.type.Type;
import egovframework.rte.itl.integration.type.TypeLoader;
import egovframework.rte.itl.webservice.data.MappingInfo;
import egovframework.rte.itl.webservice.data.WebServiceClientDefinition;
import egovframework.rte.itl.webservice.data.WebServiceServerDefinition;
import egovframework.rte.itl.webservice.data.dao.WebServiceClientDefinitionDao;
import egovframework.rte.itl.webservice.data.dao.WebServiceServerDefinitionDao;
import egovframework.rte.itl.webservice.service.EgovWebServiceClassLoader;
import egovframework.rte.itl.webservice.service.ServiceBridge;
import egovframework.rte.itl.webservice.service.ServiceEndpointInfo;
import egovframework.rte.itl.webservice.service.ServiceEndpointInterfaceInfo;

import javax.xml.ws.Holder;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class EgovWebServiceContextTest {

	private final OrganizationDefinition organizationA = new OrganizationDefinition("ORG00000", "Organization A");

	private final SystemDefinition systemA = new SystemDefinition("SYS00000", organizationA, "SYS00000", "System A", true);

	private final ServiceDefinition serviceA = new ServiceDefinition("SRV00000", systemA, "SRV00000", "Service A", "Req", "Res", "providerBean", true, true);

	private final OrganizationDefinition organizationB = new OrganizationDefinition("ORG00001", "Organization B");

	private final SystemDefinition systemB = new SystemDefinition("SYS00001", organizationB, "SYS00001", "System B", true);

	private final ServiceDefinition serviceB = new ServiceDefinition("SRV00001", systemB, "SRV00001", "Service B", "Req", "Res", "providerBean", true, true);

	private final IntegrationDefinition integrationDefinitionAtoB = new IntegrationDefinition("AtoB", serviceB, systemA, 5000, true, null, null);

	private final IntegrationDefinition integrationDefinitionBtoA = new IntegrationDefinition("BtoA", serviceA, systemB, 5000, true, null, null);

	private final RecordType recordTypeReq = new RecordType("Req", "Req");

	private final RecordType recordTypeRes = new RecordType("Res", "Res");

	private final WebServiceClientDefinition webServiceClientDefinitionA = new WebServiceClientDefinition("SRV00000", serviceA, "http://test/", "http://localhost/A/test?wsdl",
			"ServiceA", "PortA", "service", new HashMap<String, MappingInfo>(), new HashMap<String, MappingInfo>());

	private final WebServiceClientDefinition webServiceClientDefinitionB = new WebServiceClientDefinition("SRV00001", serviceB, "http://test/", "http://localhost/B/test?wsdl",
			"ServiceB", "PortB", "service", new HashMap<String, MappingInfo>(), new HashMap<String, MappingInfo>());

	private final WebServiceServerDefinition webServiceServerDefinitionA = new WebServiceServerDefinition("SRV00000", serviceA, "http://test/", "/A/test", "ServiceA", "PortA",
			"service");

	private final WebServiceServerDefinition webServiceServerDefinitionB = new WebServiceServerDefinition("SRV00001", serviceB, "http://test/", "/B/test", "ServiceB", "PortB",
			"service");

	private final EgovIntegrationServiceProvider providerBean = new EgovIntegrationServiceProvider() {
		public void service(EgovIntegrationMessage arg0, EgovIntegrationMessage arg1) {
		}
	};

	private ApplicationContext applicationContext = new AbstractApplicationContext() {
		@Override
		public Object getBean(String name) throws BeansException {
			return providerBean;
		}

		@Override
		protected void closeBeanFactory() {
		}

		@Override
		public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
			return null;
		}

		@Override
		protected void refreshBeanFactory() throws BeansException, IllegalStateException {
		}
	};

	private EgovWebServiceClassLoader classLoader = new EgovWebServiceClassLoader() {
		public String getFieldNameOfServiceBridge() {
			return "serviceBridge";
		}

		public Class<?> loadClass(ServiceEndpointInfo serviceEndpointInfo) throws ClassNotFoundException {
			if (serviceEndpointInfo.getServiceName().equals(webServiceServerDefinitionA.getServiceName())) {
				return WebServiceServerA.class;
			} else if (serviceEndpointInfo.getServiceName().equals(webServiceServerDefinitionB.getServiceName())) {
				return WebServiceServerB.class;
			}
			return null;
		}

		public Class<?> loadClass(ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo) throws ClassNotFoundException {
			if (serviceEndpointInterfaceInfo.getServiceName().equals(webServiceClientDefinitionA.getServiceName())) {
				return WebServiceClientA.class;
			} else if (serviceEndpointInterfaceInfo.getServiceName().equals(webServiceClientDefinitionB.getServiceName())) {
				return WebServiceClientB.class;
			}
			return null;
		}

		public Class<?> loadClass(Type type) throws ClassNotFoundException {
			if (type.getId().equals("Req")) {
				return Req.class;
			} else if (type.getId().equals("Res")) {
				return Res.class;
			}
			return null;
		}
	};

	private IntegrationDefinitionDao integrationDefinitionDao = new IntegrationDefinitionDao() {
		private final Map<String, IntegrationDefinition> map = new HashMap<String, IntegrationDefinition>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 9216266293744163667L;

			{
				put(integrationDefinitionAtoB.getId(), integrationDefinitionAtoB);
				put(integrationDefinitionBtoA.getId(), integrationDefinitionBtoA);
			}
		};

		private final List<IntegrationDefinition> consumerA = new ArrayList<IntegrationDefinition>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 5493765656184047738L;

			{
				add(integrationDefinitionAtoB);
			}
		};

		private final List<IntegrationDefinition> consumerB = new ArrayList<IntegrationDefinition>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 8097239060854499645L;

			{
				add(integrationDefinitionBtoA);
			}
		};

		public IntegrationDefinition getIntegrationDefinition(String arg0) {
			return map.get(arg0);
		}

		public List<IntegrationDefinition> getIntegrationDefinitionOfConsumer(String arg0, String arg1) {
			if (arg0.equals(organizationA.getId())) {
				return consumerA;
			} else if (arg0.equals(organizationB.getId())) {
				return consumerB;
			}
			return null;
		}

		public List<IntegrationDefinition> getIntegrationDefinitionOfProvider(String arg0, String arg1) {
			if (arg0.equals(organizationA.getId())) {
				return consumerB;
			} else if (arg0.equals(organizationB.getId())) {
				return consumerA;
			}
			return null;
		}
	};

	private WebServiceClientDefinitionDao webServiceClientDefinitionDao = new WebServiceClientDefinitionDao() {
		private Map<String, WebServiceClientDefinition> map = new HashMap<String, WebServiceClientDefinition>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 1004929920935330233L;

			{
				put(webServiceClientDefinitionA.getKey(), webServiceClientDefinitionA);
				put(webServiceClientDefinitionB.getKey(), webServiceClientDefinitionB);
			}
		};

		public WebServiceClientDefinition getWebServiceClientDefinition(ServiceDefinition serviceDefinition) {
			return getWebServiceClientDefinition(serviceDefinition.getKey());
		}

		public WebServiceClientDefinition getWebServiceClientDefinition(String key) {
			return map.get(key);
		}
	};

	private WebServiceServerDefinitionDao webServiceServerDefinitionDao = new WebServiceServerDefinitionDao() {
		private Map<String, WebServiceServerDefinition> map = new HashMap<String, WebServiceServerDefinition>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = 132618414423619723L;

			{
				put(webServiceServerDefinitionA.getKey(), webServiceServerDefinitionA);
				put(webServiceServerDefinitionB.getKey(), webServiceServerDefinitionB);
			}
		};

		public WebServiceServerDefinition getWebServiceServerDefinition(ServiceDefinition serviceDefinition) {
			return getWebServiceServerDefinition(serviceDefinition.getKey());
		}

		public WebServiceServerDefinition getWebServiceServerDefinition(String key) {
			return map.get(key);
		}
	};

	private TypeLoader typeLoader = new TypeLoader() {
		private Map<String, Type> map = new HashMap<String, Type>() {
			/**
			 *  serialVersion UID
			 */
			private static final long serialVersionUID = -1385594051029520247L;

			{
				put(recordTypeReq.getId(), recordTypeReq);
				put(recordTypeRes.getId(), recordTypeRes);
			}
		};

		public Type getType(String arg0) {
			return map.get(arg0);
		}
	};

	@Test
	public void testCreation() throws Exception {
		EgovWebServiceContext context = new EgovWebServiceContext();
		context.setApplicationContext(applicationContext);
		context.setClassLoader(classLoader);
		context.setDefaultTimeout(5000);
		context.setIntegrationDefinitionDao(integrationDefinitionDao);
		context.setOrganizationId(organizationA.getId());
		context.setSystemId(systemA.getId());
		context.setTypeLoader(typeLoader);
		context.setWebServiceClientDefinitionDao(webServiceClientDefinitionDao);
		context.setWebServiceServerDefinitionDao(webServiceServerDefinitionDao);
		context.init();
	}
}

class Req {
}

class Res {
}

interface WebServiceClientA {
	public void service(Holder<EgovWebServiceMessageHeader> header);
}

interface WebServiceClientB {
	public void service(Holder<EgovWebServiceMessageHeader> header);
}

class WebServiceServerA implements WebServiceClientA {
	public ServiceBridge serviceBridge;

	public void service(Holder<EgovWebServiceMessageHeader> header) {
	}
}

class WebServiceServerB implements WebServiceClientB {
	public ServiceBridge serviceBridge;

	public void service(Holder<EgovWebServiceMessageHeader> header) {
	}
}