package org.egovframe.rte.psl.reactive.r2dbc.audit;

import org.egovframe.rte.psl.reactive.r2dbc.entity.EgovReactiveBaseEntity;
import org.egovframe.rte.psl.reactive.r2dbc.repository.EgovR2dbcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.util.context.Context;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.relational.core.query.Criteria.where;

/**
 * R2DBC 감사 필드·감사자 테스트 — 저장 시 등록 필드, 수정 시 수정 필드만 갱신, Context 사용자 유무, 컬럼 규약.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = R2dbcAuditingConfiguration.class)
class EgovReactiveAuditingTest {

	@Autowired
	private EgovR2dbcRepository<AuditedSample> repository;

	private AuditedSample reload(Integer id) {
		return repository.selectOneData(Query.query(where("id").is(id)), AuditedSample.class).block();
	}

	@Test
	void 저장_시_Context_사용자와_일시가_기입되고_수정_시_수정_필드만_바뀐다() {
		AuditedSample saved = repository.insertData(new AuditedSample("first"))
				.contextWrite(Context.of(EgovContextReactiveAuditorAware.USER_ID_KEY, "hong"))
				.block();
		assertNotNull(saved);
		assertNotNull(saved.getId());

		AuditedSample loaded = reload(saved.getId());
		assertEquals("hong", loaded.getCreatedBy());
		assertNotNull(loaded.getCreatedDate());
		assertEquals("hong", loaded.getModifiedBy(), "생성 시점에는 수정자도 함께 채워진다(Spring Data 기본)");
		assertNotNull(loaded.getModifiedDate());
		LocalDateTime createdDate = loaded.getCreatedDate();

		loaded.setName("second");
		repository.updateData(loaded)
				.contextWrite(Context.of(EgovContextReactiveAuditorAware.USER_ID_KEY, "kim"))
				.block();

		AuditedSample reloaded = reload(saved.getId());
		assertEquals("second", reloaded.getName());
		assertEquals("hong", reloaded.getCreatedBy(), "등록자는 그대로");
		assertEquals(createdDate, reloaded.getCreatedDate(), "등록일시는 그대로");
		assertEquals("kim", reloaded.getModifiedBy(), "수정자는 이번 구독의 사용자");
		assertFalse(reloaded.getModifiedDate().isBefore(createdDate));
	}

	@Test
	void Context_에_사용자가_없으면_등록자는_비고_일시는_기입된다() {
		AuditedSample saved = repository.insertData(new AuditedSample("no-user")).block();
		assertNotNull(saved);

		AuditedSample loaded = reload(saved.getId());
		assertNull(loaded.getCreatedBy(), "임의 대체값을 만들지 않는다");
		assertNotNull(loaded.getCreatedDate());
		assertNull(loaded.getModifiedBy());
		assertNotNull(loaded.getModifiedDate());
	}

	@Test
	void 감사자는_Context_의_userId_를_돌려주고_없거나_비어_있으면_빈_Mono_다() {
		EgovContextReactiveAuditorAware auditorAware = new EgovContextReactiveAuditorAware();
		assertEquals("hong", auditorAware.getCurrentAuditor()
				.contextWrite(Context.of(EgovContextReactiveAuditorAware.USER_ID_KEY, "hong")).block());
		assertEquals(Optional.empty(), auditorAware.getCurrentAuditor().blockOptional());
		assertEquals(Optional.empty(), auditorAware.getCurrentAuditor()
				.contextWrite(Context.of(EgovContextReactiveAuditorAware.USER_ID_KEY, "  ")).blockOptional());
	}

	@Test
	void 컬럼_이름과_시각_타입은_JPA_규약과_같다() {
		Map<String, String> columns = new LinkedHashMap<>();
		for (Field field : EgovReactiveBaseEntity.class.getDeclaredFields()) {
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				columns.put(field.getName(), column.value() + ":" + field.getType().getSimpleName());
			}
		}
		assertEquals(Map.of(
				"createdBy", "created_by:String",
				"createdDate", "created_date:LocalDateTime",
				"modifiedBy", "modified_by:String",
				"modifiedDate", "modified_date:LocalDateTime"), columns);
		// 세터 없음 — 업무 코드 임의 조작 금지
		for (java.lang.reflect.Method method : EgovReactiveBaseEntity.class.getDeclaredMethods()) {
			assertTrue(!method.getName().startsWith("set"), "세터가 없어야 한다: " + method.getName());
		}
	}
}
