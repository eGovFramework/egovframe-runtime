/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.ai.langchain4j.memory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

/**
 * JDBC 채팅 메모리 저장소 — LangChain4j {@link ChatMemoryStore} 의 관계형 DB 구현(LangChain4j 는 InMemory 만 제공).
 *
 * <p>메모리 id 당 한 행에 LangChain4j 직렬화기({@link ChatMessageSerializer}/{@link ChatMessageDeserializer})의 JSON 을 저장한다 — 메시지
 * 타입별 컬럼 매핑을 직접 만들지 않으므로 도구 호출·이미지 등 모든 메시지 타입이 왕복한다(샘플 JPA 저장소는 미지 타입을 경고 후
 * 버렸다). 갱신은 UPDATE 후 0건이면 INSERT(방언 비종속), 저장소 오류는 삼키지 않고 전파한다. 테이블은 {@code egov/ai/langchain4j/
 * schema-ansi.sql}·{@code schema-hsqldb.sql} 스크립트로 운영자가 만든다 — <b>기동 시 DDL 을 자동 실행하지 않는다.</b> Oracle 은 4,000자
 * 초과 CLOB 바인딩 방식을 방언에 맞게 확인한다. optional 의존 {@code spring-jdbc} 가 필요하다.</p>
 *
 * <p><b>보안 — memoryId 는 접근 제어 키다.</b> 이 저장소는 memoryId 만으로 대화를 읽고·덮어쓰고·지운다(소유자 검사 없음).
 * 클라이언트가 보낸 대화 id(conversationId 등)를 검증 없이 {@code @MemoryId} 로 넘기면 다른 사용자의 대화 이력을 열람·변조할 수
 * 있다(CWE-639). memoryId 는 <b>인증된 주체에 귀속된 값</b>(예: 서버가 발급해 세션·사용자에 묶은 UUID, 또는 {@code userId + ":" +
 * conversationId} 처럼 인증 주체를 접두로 결합한 값)으로 앱이 만들어야 하며, 형식은 영숫자·{@code ._:@-} 128자 이내로 강제한다
 * (2026-09-07 보안 재점검).</p>
 *
 * <pre>
 * &lt;&lt; 개정이력(Modification Information) &gt;&gt;
 *
 *  수정일        수정자        수정내용
 *  ----------  --------    ---------------------------
 *  2026.09.03  실행환경팀     최초 생성 (과제 23 add-ai-langchain4j — 저장소 SPI 관례: 자동 DDL 없음·오류 전파)
 *  2026.09.07  실행환경팀     보안 재점검(4차): memoryId 형식 강제 + 소유자 귀속 경고(IDOR)
 * </pre>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 */
public final class EgovJdbcChatMemoryStore implements ChatMemoryStore {

	/** 기본 테이블. */
	public static final String DEFAULT_TABLE = "EGOV_AI_CHAT_MEMORY";

	private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*");

	/** memoryId 허용 형식 — 영숫자·점·밑줄·콜론·@·하이픈, 1~128자 */
	private static final Pattern MEMORY_ID = Pattern.compile("[A-Za-z0-9._:@\\-]{1,128}");

	private final JdbcTemplate jdbc;
	private final String selectSql;
	private final String updateSql;
	private final String insertSql;
	private final String deleteSql;

	/**
	 * 기본 테이블의 저장소.
	 *
	 * @param jdbc JdbcTemplate
	 */
	public EgovJdbcChatMemoryStore(JdbcTemplate jdbc) {
		this(jdbc, DEFAULT_TABLE);
	}

	/**
	 * 테이블을 지정한 저장소.
	 *
	 * @param jdbc JdbcTemplate
	 * @param table 테이블 이름(SQL 식별자)
	 */
	public EgovJdbcChatMemoryStore(JdbcTemplate jdbc, String table) {
		this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
		if (table == null || !IDENTIFIER.matcher(table).matches()) {
			throw new IllegalArgumentException("table 은 SQL 식별자여야 합니다: " + table);
		}
		this.selectSql = "SELECT MESSAGES FROM " + table + " WHERE MEMORY_ID = ?";
		this.updateSql = "UPDATE " + table + " SET MESSAGES = ?, UPDATED_AT = ? WHERE MEMORY_ID = ?";
		this.insertSql = "INSERT INTO " + table + " (MEMORY_ID, MESSAGES, UPDATED_AT) VALUES (?, ?, ?)";
		this.deleteSql = "DELETE FROM " + table + " WHERE MEMORY_ID = ?";
	}

	@Override
	public List<ChatMessage> getMessages(Object memoryId) {
		List<String> rows = jdbc.queryForList(selectSql, String.class, key(memoryId));
		if (rows.isEmpty() || rows.get(0) == null || rows.get(0).isBlank()) {
			return List.of();
		}
		return ChatMessageDeserializer.messagesFromJson(rows.get(0));
	}

	@Override
	public void updateMessages(Object memoryId, List<ChatMessage> messages) {
		String id = key(memoryId);
		String json = ChatMessageSerializer.messagesToJson(messages == null ? List.of() : messages);
		Timestamp now = Timestamp.from(Instant.now());
		int updated = jdbc.update(updateSql, json, now, id);
		if (updated == 0) {
			jdbc.update(insertSql, id, json, now);
		}
	}

	@Override
	public void deleteMessages(Object memoryId) {
		jdbc.update(deleteSql, key(memoryId));
	}

	private static String key(Object memoryId) {
		Objects.requireNonNull(memoryId, "memoryId must not be null");
		String key = String.valueOf(memoryId);
		if (key.isBlank()) {
			throw new IllegalArgumentException("memoryId 는 비어 있을 수 없습니다");
		}
		if (!MEMORY_ID.matcher(key).matches()) {
			// 클라이언트가 보낸 값이 그대로 저장 키가 되는 자리 — 형식을 강제해 공백·개행·구분자 섞인 키를 막는다(입력값 비반사)
			throw new IllegalArgumentException("memoryId 는 영숫자·._:@- 로 된 128자 이내 값이어야 합니다");
		}
		return key;
	}
}
