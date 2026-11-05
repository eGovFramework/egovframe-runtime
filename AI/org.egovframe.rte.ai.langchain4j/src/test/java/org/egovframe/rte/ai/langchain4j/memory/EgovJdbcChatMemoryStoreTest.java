package org.egovframe.rte.ai.langchain4j.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * JDBC 채팅 메모리 저장소 계약 테스트 — 직렬화 왕복·UPDATE→INSERT·삭제·미존재·오류 전파·스크립트 표(스펙 ai/langchain4j-chat-memory-store).
 */
class EgovJdbcChatMemoryStoreTest {

	private static JdbcTemplate jdbc;

	@BeforeAll
	static void schema() throws SQLException {
		JDBCDataSource dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:mem:lc4j_chat_memory");
		dataSource.setUser("SA");
		dataSource.setPassword("");
		try (Connection connection = dataSource.getConnection()) {
			ScriptUtils.executeSqlScript(connection, new ClassPathResource("egov/ai/langchain4j/schema-hsqldb.sql"));
		}
		jdbc = new JdbcTemplate(dataSource);
	}

	@Test
	@DisplayName("사용자·어시스턴트·시스템 메시지 3건이 같은 타입·텍스트로 왕복한다")
	void roundTrip() {
		EgovJdbcChatMemoryStore store = new EgovJdbcChatMemoryStore(jdbc);
		List<ChatMessage> messages = List.of(SystemMessage.from("너는 도우미다"), UserMessage.from("페이징 설정 방법은?"), AiMessage.from("PaginationInfo 로 설정한다."));

		store.updateMessages("conv-round", messages);
		List<ChatMessage> restored = store.getMessages("conv-round");

		assertEquals(3, restored.size());
		assertEquals("너는 도우미다", ((SystemMessage) restored.get(0)).text());
		assertEquals("페이징 설정 방법은?", ((UserMessage) restored.get(1)).singleText());
		assertEquals("PaginationInfo 로 설정한다.", ((AiMessage) restored.get(2)).text());
	}

	@Test
	@DisplayName("memoryId 는 영숫자·._:@- 128자 이내여야 한다 — 클라이언트 값이 그대로 키가 되는 자리라 형식을 강제한다(2026-09-07 4차 점검)")
	void memoryIdFormatIsEnforced() {
		EgovJdbcChatMemoryStore store = new EgovJdbcChatMemoryStore(jdbc);
		assertThrows(IllegalArgumentException.class, () -> store.getMessages("conv\nother"));
		assertThrows(IllegalArgumentException.class, () -> store.getMessages("a".repeat(129)));
		assertThrows(IllegalArgumentException.class, () -> store.updateMessages("conv other", List.of(UserMessage.from("x"))));
		assertThrows(IllegalArgumentException.class, () -> store.deleteMessages("conv;drop"));
		assertTrue(store.getMessages("user@korea.kr:session-1").isEmpty());
	}

	@Test
	@DisplayName("두 번째 갱신은 UPDATE 로 덮어쓰고 삭제 뒤 조회는 빈 목록이다")
	void updateAndDelete() {
		EgovJdbcChatMemoryStore store = new EgovJdbcChatMemoryStore(jdbc);

		store.updateMessages("conv-upd", List.of(UserMessage.from("첫 번째")));
		store.updateMessages("conv-upd", List.of(UserMessage.from("첫 번째"), AiMessage.from("두 번째")));
		assertEquals(2, store.getMessages("conv-upd").size());
		assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM EGOV_AI_CHAT_MEMORY WHERE MEMORY_ID = ?", Integer.class, "conv-upd"));

		store.deleteMessages("conv-upd");
		assertTrue(store.getMessages("conv-upd").isEmpty());
	}

	@Test
	@DisplayName("존재하지 않는 메모리 id 는 빈 목록, null·빈 id 는 거부한다")
	void missingAndInvalidIds() {
		EgovJdbcChatMemoryStore store = new EgovJdbcChatMemoryStore(jdbc);

		assertTrue(store.getMessages("never-stored").isEmpty());
		assertThrows(NullPointerException.class, () -> store.getMessages(null));
		assertThrows(IllegalArgumentException.class, () -> store.updateMessages("  ", List.of()));
	}

	@Test
	@DisplayName("저장소 오류는 그대로 전파된다(없는 테이블)")
	void propagatesStoreErrors() {
		EgovJdbcChatMemoryStore store = new EgovJdbcChatMemoryStore(jdbc, "NO_SUCH_TABLE");

		assertThrows(DataAccessException.class, () -> store.updateMessages("conv-x", List.of(UserMessage.from("x"))));
		assertThrows(IllegalArgumentException.class, () -> new EgovJdbcChatMemoryStore(jdbc, "bad name; drop"));
	}

	@Test
	@DisplayName("MessageWindowChatMemory 의 저장소로 동작한다")
	void worksWithMessageWindowChatMemory() {
		EgovJdbcChatMemoryStore store = new EgovJdbcChatMemoryStore(jdbc);
		MessageWindowChatMemory memory = MessageWindowChatMemory.builder().id("conv-window").maxMessages(10).chatMemoryStore(store).build();

		memory.add(UserMessage.from("질문"));
		memory.add(AiMessage.from("답변"));

		assertEquals(2, store.getMessages("conv-window").size());
		MessageWindowChatMemory reloaded = MessageWindowChatMemory.builder().id("conv-window").maxMessages(10).chatMemoryStore(store).build();
		assertEquals(2, reloaded.messages().size());
		assertEquals("답변", ((AiMessage) reloaded.messages().get(1)).text());
	}
}
