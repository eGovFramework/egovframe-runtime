/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.itl.webservice.service.impl;

import org.egovframe.rte.itl.integration.type.ListType;
import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.egovframe.rte.itl.integration.type.RecordType;
import org.egovframe.rte.itl.integration.type.Type;
import org.egovframe.rte.itl.webservice.EgovWebServiceMessageHeader;
import org.egovframe.rte.itl.webservice.service.EgovWebServiceClassLoader;
import org.egovframe.rte.itl.webservice.service.ServiceEndpointInfo;
import org.egovframe.rte.itl.webservice.service.ServiceEndpointInterfaceInfo;
import org.egovframe.rte.itl.webservice.service.ServiceParamInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.Holder;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static javax.jws.WebParam.Mode.INOUT;
import static javax.jws.WebParam.Mode.OUT;
import static org.objectweb.asm.Opcodes.*;

/**
 * 웹 서비스에 필요한 클래스를 생성하는 ClassLoader 구현 클래스
 * <p>
 * <b>NOTE:</b> 웹 서비스에 필요한 Type, ServiceEndpointInterface, ServiceEndpoint 등의 
 * class를 생성하는 ClassLoader이다. </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 */
public class EgovWebServiceClassLoaderImpl extends ClassLoader implements EgovWebServiceClassLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovWebServiceClassLoaderImpl.class);

	public static final String BASE_PACKAGE_NAME = "org.egovframe.rte.itl.webservice.generated";

	public static final String CLASS_NAME_PREFIX = BASE_PACKAGE_NAME + "." + "EgovType";

	public static final String SERVICE_ENDPOINT_CLASS_NAME_POSTFIX = "ServiceImpl";

	public static final String SERVICE_ENDPOINT_INTERFACE_CLASS_NAME_POSTFIX = "Service";

	public static final String NAME_OF_SERVICE_BRIDGE_CLASS = "egovframework/rte/itl/webservice/service/ServiceBridge";

	public static final String DESC_OF_SERVICE_BRIDGE_CLASS = "L" + NAME_OF_SERVICE_BRIDGE_CLASS + ";";

	public static final String FIELD_NAME_OF_SERVICE_BRIDGE = "serviceBridge";

	private static final Map<PrimitiveType, Class<?>> primitiveClasses = new HashMap<PrimitiveType, Class<?>>() {
		/**
		 * serialVersion UID
		 */
		private static final long serialVersionUID = 6485971274591326780L;
		{
			put(PrimitiveType.BOOLEAN, boolean.class);
			put(PrimitiveType.STRING, String.class);
			put(PrimitiveType.BYTE, byte.class);
			put(PrimitiveType.SHORT, short.class);
			put(PrimitiveType.INTEGER, int.class);
			put(PrimitiveType.LONG, long.class);
			put(PrimitiveType.BIGINTEGER, BigInteger.class);
			put(PrimitiveType.FLOAT, float.class);
			put(PrimitiveType.DOUBLE, double.class);
			put(PrimitiveType.BIGDECIMAL, BigDecimal.class);
			put(PrimitiveType.CALENDAR, Calendar.class);
		}
	};

	private static final Map<Class<?>, Class<?>> wrapperClasses = new HashMap<Class<?>, Class<?>>() {
		/**
		 * serialVersion UID
		 */
		private static final long serialVersionUID = 1357144024299993247L;
		{
			put(boolean.class, Boolean.class);
			put(byte.class, Byte.class);
			put(short.class, Short.class);
			put(int.class, Integer.class);
			put(long.class, Long.class);
			put(float.class, Float.class);
			put(double.class, Double.class);
		}
	};

	/**
	 * Default Constructor
	 */
	protected EgovWebServiceClassLoaderImpl() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent
	 */
	protected EgovWebServiceClassLoaderImpl(ClassLoader parent) {
		super(parent);
	}

	public String getFieldNameOfServiceBridge() {
		return FIELD_NAME_OF_SERVICE_BRIDGE;
	}

	public Class<?> loadClass(final Type type) throws ClassNotFoundException {
		LOGGER.debug("loadClass of Type ({})", type);

		if (type == EgovWebServiceMessageHeader.TYPE) {
			LOGGER.debug("Type is EgovWebServiceMessageHeader.");
			return EgovWebServiceMessageHeader.class;
		}

		if (type instanceof PrimitiveType) {
			LOGGER.debug("Type is a Primitive Type");
			Class<?> clazz = primitiveClasses.get((PrimitiveType) type);
			if (clazz == null) {
				LOGGER.error("No such primitive type");
				throw new ClassNotFoundException();
			}
			return clazz;
		} else if (type instanceof ListType) {
			LOGGER.debug("Type is a List Type");
			ListType listType = (ListType) type;
			Class<?> elementClass = loadClass(listType.getElementType());
			return Array.newInstance(elementClass, 0).getClass();
		} else if (type instanceof RecordType) {
			LOGGER.debug("Type is a Record Type");
			RecordType recordType = (RecordType) type;
			String className = getRecordTypeClassName(recordType.getName());
			try {
				LOGGER.debug("Check the class \"{}\" is already loaded.", className);
				return loadClass(className);
			} catch (ClassNotFoundException e) {
				LOGGER.debug("Create a new class \"{}\"", className);
				byte[] byteCode = createRecordClass(className, recordType);
				return defineClass(className, byteCode, 0, byteCode.length);
			}
		}

		LOGGER.error("Type is invalid");
		throw new ClassNotFoundException();
	}

	public Class<?> loadClass(final ServiceEndpointInfo serviceEndpointInfo) throws ClassNotFoundException {
		LOGGER.debug("loadClass of ServiceEndpointInfo ({})", serviceEndpointInfo);

		// load interface class
		LOGGER.debug("load Interface Class");
		loadClass((ServiceEndpointInterfaceInfo) serviceEndpointInfo);
		String className = getServiceEndpointClassName(serviceEndpointInfo.getServiceName());

		try {
			LOGGER.debug("Check the class \"{}\" is already loaded", className);
			return loadClass(className);
		} catch (ClassNotFoundException e) {
			LOGGER.debug("Create a new class \"{}\"", className);
			byte[] byteCode = createServiceEndpointClass(serviceEndpointInfo);
			return defineClass(className, byteCode, 0, byteCode.length);
		}
	}

	public Class<?> loadClass(final ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo) throws ClassNotFoundException {
		LOGGER.debug("loadClass of ServiceEndpointInterfaceInfo ({})", serviceEndpointInterfaceInfo);
		String className = getServiceEndpointInterfaceClassName(serviceEndpointInterfaceInfo.getServiceName());

		try {
			LOGGER.debug("Check the class \"{}\" is already loaded.", className);
			return loadClass(className);
		} catch (ClassNotFoundException e) {
			LOGGER.debug("Create new ServiceEndpointInterface class");
			// load message type class
			ServiceParamInfo returnInfo = serviceEndpointInterfaceInfo.getReturnInfo();
			if (returnInfo != null) {
				LOGGER.debug("Load return type ({})", returnInfo.getType());
				loadClass(returnInfo.getType());
			}

			for (ServiceParamInfo info : serviceEndpointInterfaceInfo.getParamInfos()) {
				LOGGER.debug("Load param type ({})", info.getType());
				loadClass(info.getType());
			}

			LOGGER.debug("Create a new class \"{}\"", className);
			byte[] byteCode = createServiceEndpointInterfaceClass(serviceEndpointInterfaceInfo);
			return defineClass(className, byteCode, 0, byteCode.length);
		}
	}

	private static final String DESC_OF_XML_ACCESSOR_TYPE =
	// org.objectweb.asm.Type.getDescriptor(XmlAccessorType.class);
	"Ljavax/xml/bind/annotation/XmlAccessorType;";

	private static final String DESC_OF_XML_ACCESS_TYPE =
	// org.objectweb.asm.Type.getDescriptor(XmlAccessType.class);
	"Ljavax/xml/bind/annotation/XmlAccessType;";

	private static final String DESC_OF_WEB_SERVICE =
	// org.objectweb.asm.Type.getDescriptor(WebService.class);
	"Ljavax/jws/WebService;";

	private static final String DESC_OF_SOAP_BINDING =
	// org.objectweb.asm.Type.getDescriptor(SOAPBinding.class);
	"Ljavax/jws/soap/SOAPBinding;";

	private static final String DESC_OF_SOAP_BINDING_PARAMETER_STYLE =
	// org.objectweb.asm.Type.getDescriptor(SOAPBinding.ParameterStyle.class);
	"Ljavax/jws/soap/SOAPBinding$ParameterStyle;";

	private static final String DESC_OF_WEB_METHOD =
	// org.objectweb.asm.Type.getDescriptor(WebMethod.class);
	"Ljavax/jws/WebMethod;";

	private static final String DESC_OF_WEB_PARAM =
	// org.objectweb.asm.Type.getDescriptor(WebParam.class);
	"Ljavax/jws/WebParam;";

	private static final String DESC_OF_WEB_PARAM_MODE =
	// org.objectweb.asm.Type.getDescriptor(WebParam.Mode.class);
	"Ljavax/jws/WebParam$Mode;";

	private static final String DESC_OF_WEB_RESULT =
	// org.objectweb.asm.Type.getDescriptor(WebResult.class);
	"Ljavax/jws/WebResult;";

	private static final org.objectweb.asm.Type TYPE_OF_HOLDER = org.objectweb.asm.Type.getType(Holder.class);

	private byte[] createRecordClass(final String className, final RecordType recordType) throws ClassNotFoundException {
		String asmClassName = className.replace('.', '/');
		ClassWriter classWriter = new ClassWriter(0);
		classWriter.visit(V1_5,	ACC_PUBLIC,	asmClassName,null,"java/lang/Object",null);

		// Create Annotation
		AnnotationVisitor annotationVisitor = classWriter.visitAnnotation(DESC_OF_XML_ACCESSOR_TYPE, true);
		annotationVisitor.visitEnum("value", DESC_OF_XML_ACCESS_TYPE,"FIELD");
		annotationVisitor.visitEnd();

		// Create Fields
		for (Entry<String, Type> entry : recordType.getFieldTypes().entrySet()) {
			String fieldName = entry.getKey();
			Type fieldType = entry.getValue();
			Class<?> fieldTypeClass = loadClass(fieldType);
			String desc = org.objectweb.asm.Type.getDescriptor(fieldTypeClass);
			classWriter.visitField(ACC_PUBLIC, fieldName, desc,null,null);
		}

		// Create Constructor
		MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);
		methodVisitor.visitCode();
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>","()V");
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(1, 1);
		methodVisitor.visitEnd();
		classWriter.visitEnd();

		return classWriter.toByteArray();
	}

	private byte[] createServiceEndpointInterfaceClass(final ServiceEndpointInterfaceInfo serviceEndpointInterfaceInfo) throws ClassNotFoundException {
		String serviceEndpointInterfaceClassName = getServiceEndpointInterfaceClassName(serviceEndpointInterfaceInfo.getServiceName());
		String asmServiceEndpointInterfaceClassName = serviceEndpointInterfaceClassName.replace('.', '/');
		ClassWriter classWriter = new ClassWriter(0);
		classWriter.visit(V1_5,ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE,
				asmServiceEndpointInterfaceClassName,null,"java/lang/Object",null);

		// Create Annotation
		AnnotationVisitor annotationVisitor = classWriter.visitAnnotation(DESC_OF_WEB_SERVICE, true);
		annotationVisitor.visit("targetNamespace", serviceEndpointInterfaceInfo.getNamespace());
		annotationVisitor.visitEnd();
		annotationVisitor = classWriter.visitAnnotation(DESC_OF_SOAP_BINDING,true);
		annotationVisitor.visitEnum("parameterStyle", DESC_OF_SOAP_BINDING_PARAMETER_STYLE, "BARE");

		// Create Method
		ServiceParamInfo returnInfo = serviceEndpointInterfaceInfo.getReturnInfo();
		Collection<ServiceParamInfo> paramInfos = serviceEndpointInterfaceInfo.getParamInfos();

		StringBuffer desc = new StringBuffer("(");
		StringBuffer signature = new StringBuffer("(");

		for (ServiceParamInfo info : paramInfos) {
			Class<?> paramClass = loadClass(info.getType());
			org.objectweb.asm.Type paramType = org.objectweb.asm.Type.getType(paramClass);
			String paramSign = paramType.getDescriptor();
			if (info.getMode().equals(OUT) || info.getMode().equals(INOUT)) {
				if (paramClass.isPrimitive()) {
					paramClass = wrapperClasses.get(paramClass);
					paramType = org.objectweb.asm.Type.getType(paramClass);
					paramSign = paramType.getDescriptor();
				}
				paramClass = Holder.class;
				paramType = TYPE_OF_HOLDER;
				paramSign = "Ljavax/xml/ws/Holder<" + paramSign + ">;";
			}
			desc.append(paramType.getDescriptor());
			signature.append(paramSign);
		}
		desc.append(")");
		signature.append(")");
		org.objectweb.asm.Type returnType = (returnInfo == null ? returnType = org.objectweb.asm.Type.VOID_TYPE
				: org.objectweb.asm.Type.getType(loadClass(returnInfo.getType())));
		desc.append(returnType.getDescriptor());
		signature.append(returnType.getDescriptor());

		MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, serviceEndpointInterfaceInfo.getOperationName(), desc.toString(), signature.toString(),null);

		// @WebMethod
		annotationVisitor = methodVisitor.visitAnnotation(DESC_OF_WEB_METHOD,true);
		annotationVisitor.visit("operationName", serviceEndpointInterfaceInfo.getOperationName());
		annotationVisitor.visitEnd();

		// @WebResult
		if (returnInfo != null) {
			annotationVisitor = methodVisitor.visitAnnotation(DESC_OF_WEB_RESULT, true);
			annotationVisitor.visit("name", returnInfo.getName());
			annotationVisitor.visit("header", returnInfo.isHeader());
			annotationVisitor.visit("targetNamespace", serviceEndpointInterfaceInfo.getNamespace());
			annotationVisitor.visitEnd();
		}

		// @WebParam
		int index = 0;
		for (ServiceParamInfo info : serviceEndpointInterfaceInfo.getParamInfos()) {
			annotationVisitor = methodVisitor.visitParameterAnnotation(index, DESC_OF_WEB_PARAM, true);
			annotationVisitor.visit("name", info.getName());
			annotationVisitor.visitEnum("mode", DESC_OF_WEB_PARAM_MODE, info.getMode().toString());
			annotationVisitor.visit("header", info.isHeader());
			annotationVisitor.visit("targetNamespace", serviceEndpointInterfaceInfo.getNamespace());
			annotationVisitor.visitEnd();
			index++;
		}
		methodVisitor.visitEnd();

		// Class finalize
		classWriter.visitEnd();

		return classWriter.toByteArray();
	}

	private byte[] createServiceEndpointClass(final ServiceEndpointInfo serviceEndpointInfo) throws ClassNotFoundException {
		String serviceEndpointInterfaceClassName = getServiceEndpointInterfaceClassName(serviceEndpointInfo.getServiceName());
		String serviceEndpointClassName = getServiceEndpointClassName(serviceEndpointInfo.getServiceName());

		String asmServiceEndpointInterfaceClassName = serviceEndpointInterfaceClassName.replace('.', '/');
		String asmServiceEndpointClassName = serviceEndpointClassName.replace('.', '/');
		ClassWriter classWriter = new ClassWriter(0);
		classWriter.visit(V1_5, ACC_PUBLIC, asmServiceEndpointClassName,null,"java/lang/Object", new String[] { asmServiceEndpointInterfaceClassName });

		// Create Annotation
		AnnotationVisitor annotationVisitor = classWriter.visitAnnotation(DESC_OF_WEB_SERVICE, true);
		annotationVisitor.visit("endpointInterface", serviceEndpointInterfaceClassName);
		annotationVisitor.visit("targetNamespace", serviceEndpointInfo.getNamespace());
		annotationVisitor.visit("serviceName", serviceEndpointInfo.getServiceName());
		annotationVisitor.visit("portName", serviceEndpointInfo.getPortName());
		annotationVisitor.visitEnd();

		// Create Attribute
		FieldVisitor fieldVisitor = classWriter.visitField(ACC_PUBLIC, FIELD_NAME_OF_SERVICE_BRIDGE, DESC_OF_SERVICE_BRIDGE_CLASS,null,null);
		fieldVisitor.visitEnd();

		// Create Constructor
		MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);
		methodVisitor.visitCode();
		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>","()V");
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(1, 1);
		methodVisitor.visitEnd();

		// Create Method
		ServiceParamInfo returnInfo = serviceEndpointInfo.getReturnInfo();
		Collection<ServiceParamInfo> paramInfos = serviceEndpointInfo.getParamInfos();

		StringBuffer desc = new StringBuffer("(");
		StringBuffer signature = new StringBuffer("(");

		for (ServiceParamInfo info : paramInfos) {
			Class<?> paramClass = loadClass(info.getType());
			org.objectweb.asm.Type paramType = org.objectweb.asm.Type.getType(paramClass);
			String paramSign = paramType.getDescriptor();
			if (info.getMode().equals(OUT) || info.getMode().equals(INOUT)) {
				if (paramClass.isPrimitive()) {
					paramClass = wrapperClasses.get(paramClass);
					paramType = org.objectweb.asm.Type.getType(paramClass);
					paramSign = paramType.getDescriptor();
				}
				paramClass = Holder.class;
				paramType = TYPE_OF_HOLDER;
				paramSign = "Ljavax/xml/ws/Holder<" + paramSign + ">;";
			}
			desc.append(paramType.getDescriptor());
			signature.append(paramSign);
		}
		desc.append(")");
		signature.append(")");
		org.objectweb.asm.Type returnType = (returnInfo == null ? returnType = org.objectweb.asm.Type.VOID_TYPE
				: org.objectweb.asm.Type.getType(loadClass(returnInfo.getType())));
		desc.append(returnType.getDescriptor());
		signature.append(returnType.getDescriptor());

		methodVisitor = classWriter.visitMethod(ACC_PUBLIC, serviceEndpointInfo.getOperationName(), desc.toString(), signature.toString(),null);

		int mapPosition = paramInfos.size() + 1;
		methodVisitor.visitCode();
		methodVisitor.visitTypeInsn(NEW, "java/util/HashMap");
		methodVisitor.visitInsn(DUP);
		methodVisitor.visitMethodInsn(INVOKESPECIAL,"java/util/HashMap","<init>","()V");
		methodVisitor.visitVarInsn(ASTORE, mapPosition);

		int i = 1;
		for (ServiceParamInfo info : paramInfos) {
			methodVisitor.visitVarInsn(ALOAD, mapPosition);
			methodVisitor.visitLdcInsn(info.getName());
			methodVisitor.visitVarInsn(ALOAD, i);
			methodVisitor.visitMethodInsn(INVOKEINTERFACE,"java/util/Map","put","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
			methodVisitor.visitInsn(POP);
			i++;
		}

		methodVisitor.visitVarInsn(ALOAD, 0);
		methodVisitor.visitFieldInsn(GETFIELD, asmServiceEndpointClassName, FIELD_NAME_OF_SERVICE_BRIDGE, DESC_OF_SERVICE_BRIDGE_CLASS);
		methodVisitor.visitVarInsn(ALOAD, mapPosition);
		methodVisitor.visitMethodInsn(INVOKEINTERFACE, NAME_OF_SERVICE_BRIDGE_CLASS,"doService","(Ljava/util/Map;)Ljava/lang/Object;");

		if (returnInfo != null) {
			methodVisitor.visitTypeInsn(CHECKCAST, returnType.getInternalName());
			methodVisitor.visitInsn(ARETURN);
		} else {
			methodVisitor.visitInsn(POP);
			methodVisitor.visitInsn(RETURN);
		}

		methodVisitor.visitMaxs(paramInfos.size(), paramInfos.size() + 2);
		methodVisitor.visitEnd();

		// Class finalize
		classWriter.visitEnd();

		return classWriter.toByteArray();
	}

	private static String getRecordTypeClassName(final String recordTypeName) {
		return CLASS_NAME_PREFIX + recordTypeName;
	}

	private static String getServiceEndpointClassName(final String serviceName) {
		return CLASS_NAME_PREFIX + serviceName + SERVICE_ENDPOINT_CLASS_NAME_POSTFIX;
	}

	private static String getServiceEndpointInterfaceClassName(final String serviceName) {
		return CLASS_NAME_PREFIX + serviceName + SERVICE_ENDPOINT_INTERFACE_CLASS_NAME_POSTFIX;
	}

}
