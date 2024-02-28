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
package org.egovframe.rte.psl.reactive.mongodb.connect;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

/**
 * MongoDB 데이터베이스 연동 클래스
 *
 * <p>Desc.: MongoDB 데이터베이스 연동 클래스</p>
 *
 * @author ESFC
 * @since 2023.08.31
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   ESFC            최초 생성
 * </pre>
 */
public class EgovMongoDbConnectionFactory {

    private final String mongoDbName;
    private final String mongoDbUrl;

    public EgovMongoDbConnectionFactory(String mongoDbName, String mongoDbUrl) {
        this.mongoDbName = mongoDbName;
        this.mongoDbUrl = mongoDbUrl;
    }

    public ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory() {
        ConnectionString connectionString = new ConnectionString(this.mongoDbUrl);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        return new SimpleReactiveMongoDatabaseFactory(MongoClients.create(mongoClientSettings), this.mongoDbName);
    }

}
