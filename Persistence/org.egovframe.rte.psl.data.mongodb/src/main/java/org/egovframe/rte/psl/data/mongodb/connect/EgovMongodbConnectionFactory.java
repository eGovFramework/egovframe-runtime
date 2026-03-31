/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.psl.data.mongodb.connect;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * MongoDB 데이터베이스 연동 클래스
 *
 * <p>Desc.: MongoDB 데이터베이스 연동 클래스</p>
 *
 * @author 유지보수
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2023.08.31   유지보수            최초 생성
 * </pre>
 * @since 2023.08.31
 */
public class EgovMongodbConnectionFactory {

    private final String mongoDbName;
    private final String mongoDbUrl;

    public EgovMongodbConnectionFactory(String mongoDbName, String mongoDbUrl) {
        this.mongoDbName = mongoDbName;
        this.mongoDbUrl = mongoDbUrl;
    }

    public MongoDatabaseFactory mongoDbFactory() {
        ConnectionString connectionString = new ConnectionString(this.mongoDbUrl);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return new SimpleMongoClientDatabaseFactory(MongoClients.create(mongoClientSettings), this.mongoDbName);
    }

}
