package org.egovframe.rte.itl.integration.type.support;

import org.egovframe.rte.itl.integration.metadata.RecordTypeDefinition;
import org.egovframe.rte.itl.integration.metadata.RecordTypeFieldDefinition;
import org.egovframe.rte.itl.integration.metadata.dao.RecordTypeDefinitionDao;
import org.egovframe.rte.itl.integration.type.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
public class TypeLoaderUsingMetadataTest {
    private static TypeLoaderUsingMetadata typeLoader;

    @BeforeEach
    public void beforeClass() throws Exception {

        RecordTypeDefinitionDao recordTypeDefinitionDao = new RecordTypeDefinitionDao() {
            private final RecordTypeDefinition header = new RecordTypeDefinition("header", "header", null, new HashMap<String, RecordTypeFieldDefinition>() {
                /**
                 *  serialVersion UID
                 */
                private static final long serialVersionUID = -7181896004770582181L;

                {
                    put("serviceId", new RecordTypeFieldDefinition("string"));
                }
            });

            private final RecordTypeDefinition recordJuminCheck = new RecordTypeDefinition("recordJuminCheck", "recordJuminCheck", null,
                    new HashMap<String, RecordTypeFieldDefinition>() {
                        /**
                         *  serialVersion UID
                         */
                        private static final long serialVersionUID = 5773848040359641226L;

                        {
                            put("juminNo", new RecordTypeFieldDefinition("string"));
                            put("name", new RecordTypeFieldDefinition("string"));
                            put("children", new RecordTypeFieldDefinition("recordJuminCheckList"));
                        }
                    });

            private final RecordTypeDefinition recordJuminCheckList = new RecordTypeDefinition("recordJuminCheckList", "recordJuminCheckList", null,
                    new HashMap<String, RecordTypeFieldDefinition>() {
                        /**
                         *  serialVersion UID
                         */
                        private static final long serialVersionUID = -8731641108089143386L;

                        {
                            //                                put("list", new RecordTypeFieldDefinition("list", "recordJuminCheck[]"));
                            put("list", new RecordTypeFieldDefinition("recordJuminCheck[]"));
                        }
                    });

            private final RecordTypeDefinition messageJuminCheck = new RecordTypeDefinition("messageJuminCheck", "messageJuminCheck", header,
                    new HashMap<String, RecordTypeFieldDefinition>() {
                        /**
                         *  serialVersion UID
                         */
                        private static final long serialVersionUID = -6827218667391768161L;

                        {
                            //                                put("body", new RecordTypeFieldDefinition("body", "recordJuminCheck"));
                            put("body", new RecordTypeFieldDefinition("recordJuminCheck"));
                        }
                    });

            private final RecordTypeDefinition messageJuminCheckList = new RecordTypeDefinition("messageJuminCheckList", "messageJuminChecklist", header,
                    new HashMap<String, RecordTypeFieldDefinition>() {
                        /**
                         *  serialVersion UID
                         */
                        private static final long serialVersionUID = -8701473491681953372L;

                        {
                            //                                put("serviceId", new RecordTypeFieldDefinition("serviceId", "integer"));
                            //                                put("body", new RecordTypeFieldDefinition("body", "recordJuminCheckList"));
                            put("serviceId", new RecordTypeFieldDefinition("integer"));
                            put("body", new RecordTypeFieldDefinition("recordJuminCheckList"));
                        }
                    });

            private final RecordTypeDefinition messageCircularA = new RecordTypeDefinition("messageCircularA", "messageCircularA", null,
                    new HashMap<String, RecordTypeFieldDefinition>() {
                        /**
                         *  serialVersion UID
                         */
                        private static final long serialVersionUID = -3390525326521171121L;

                        {
                            //                                put("data", new RecordTypeFieldDefinition("data", "integer"));
                            put("data", new RecordTypeFieldDefinition("integer"));
                        }
                    });

            private final RecordTypeDefinition messageCircularB = new RecordTypeDefinition("messageCircularB", "messageCircularB", messageCircularA,
                    new HashMap<String, RecordTypeFieldDefinition>() {
                        /**
                         *  serialVersion UID
                         */
                        private static final long serialVersionUID = -4010838890333553878L;

                        {
                            //                                put("data", new RecordTypeFieldDefinition("data", "integer"));
                            put("data", new RecordTypeFieldDefinition("integer"));
                        }
                    });

            private final RecordTypeDefinition messageCircularC = new RecordTypeDefinition("messageCircularC", "messageCircularC", messageCircularA,
                    new HashMap<String, RecordTypeFieldDefinition>() {
                        /**
                         *  serialVersion UID
                         */
                        private static final long serialVersionUID = -1370456063910326717L;

                        {
                            //                                put("data", new RecordTypeFieldDefinition("data", "integer"));
                            put("data", new RecordTypeFieldDefinition("integer"));
                        }
                    });

            private final Map<String, RecordTypeDefinition> map = new HashMap<String, RecordTypeDefinition>() {
                /**
                 *  serialVersion UID
                 */
                private static final long serialVersionUID = 3173940718644980847L;

                {
                    put(header.getId(), header);
                    put(recordJuminCheck.getId(), recordJuminCheck);
                    put(recordJuminCheckList.getId(), recordJuminCheckList);
                    put(messageJuminCheck.getId(), messageJuminCheck);
                    put(messageJuminCheckList.getId(), messageJuminCheckList);
                    messageCircularA.setParent(messageCircularC);
                    put(messageCircularA.getId(), messageCircularA);
                    put(messageCircularB.getId(), messageCircularB);
                    put(messageCircularC.getId(), messageCircularC);
                }
            };

            public RecordTypeDefinition getRecordTypeDefinition(String id) {
                return map.get(id);
            }
        };

        typeLoader = new TypeLoaderUsingMetadata();
        typeLoader.setRecordTypeDefinitionDao(recordTypeDefinitionDao);
    }

    @Test
    public void testPrimitiveType() {
        assertEquals(PrimitiveType.BOOLEAN, typeLoader.getType("boolean"));
        assertEquals(PrimitiveType.STRING, typeLoader.getType("string"));
        assertEquals(PrimitiveType.BYTE, typeLoader.getType("byte"));
        assertEquals(PrimitiveType.SHORT, typeLoader.getType("short"));
        assertEquals(PrimitiveType.INTEGER, typeLoader.getType("integer"));
        assertEquals(PrimitiveType.LONG, typeLoader.getType("long"));
        assertEquals(PrimitiveType.BIGINTEGER, typeLoader.getType("biginteger"));
        assertEquals(PrimitiveType.FLOAT, typeLoader.getType("float"));
        assertEquals(PrimitiveType.DOUBLE, typeLoader.getType("double"));
        assertEquals(PrimitiveType.BIGDECIMAL, typeLoader.getType("bigdecimal"));
        assertEquals(PrimitiveType.CALENDAR, typeLoader.getType("calendar"));
    }

    @Test
    public void testSimpleRecord() {
        String id = "header";
        Type type = typeLoader.getType(id);
        assertEquals(id, type.getId());
        assertEquals(RecordType.class, type.getClass());
        assertEquals(PrimitiveType.STRING, ((RecordType) type).getFieldType("serviceId"));
    }

    @Test
    public void testComplexRecord() {
        String id = "messageJuminCheck";
        Type type = typeLoader.getType(id);
        assertEquals(id, type.getId());
        assertEquals(RecordType.class, type.getClass());
        RecordType recordType = (RecordType) type;
        assertEquals(PrimitiveType.STRING, recordType.getFieldType("serviceId"));
        assertEquals(typeLoader.getType("recordJuminCheck"), recordType.getFieldType("body"));
    }

    @Test
    public void testInheritance() {
        String id = "messageJuminCheckList";
        Type type = typeLoader.getType(id);
        assertEquals(id, type.getId());
        assertEquals(RecordType.class, type.getClass());
        RecordType recordType = (RecordType) type;
        assertEquals(PrimitiveType.INTEGER, recordType.getFieldType("serviceId"));
    }

    @Test
    public void testCircularReference() {
        String id = "messageJuminCheckList";
        Type type = typeLoader.getType(id);
        assertEquals(id, type.getId());
        assertEquals(RecordType.class, type.getClass());
        RecordType recordType = (RecordType) type;
        Type bodyFieldType = recordType.getFieldType("body");
        assertEquals(RecordType.class, bodyFieldType.getClass());
        RecordType bodyRecordType = (RecordType) bodyFieldType;
        Type listFieldType = bodyRecordType.getFieldType("list");
        assertEquals(ListType.class, listFieldType.getClass());
        assertEquals(typeLoader.getType("recordJuminCheck"), ((ListType) listFieldType).getElementType());
        Type elementType = ((ListType) listFieldType).getElementType();
        assertEquals(RecordType.class, elementType.getClass());
        RecordType elementRecordType = (RecordType) elementType;
        assertEquals(bodyFieldType, elementRecordType.getFieldType("children"));
    }

    @Test
    public void testNoSuchTypeException() {
        try {
            typeLoader.getType("noMessage");
            fail();
        } catch (NoSuchTypeException e) {
            // Success
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCircularInheritanceException() {
        try {
            typeLoader.getType("messageCircularA");
            fail();
        } catch (CircularInheritanceException e) {
            // Success
        } catch (Exception e) {
            fail();
        }
    }

}
