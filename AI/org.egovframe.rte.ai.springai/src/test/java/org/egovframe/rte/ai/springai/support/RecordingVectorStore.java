package org.egovframe.rte.ai.springai.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

/**
 * 기록형 가짜 벡터 저장소 — 추가 배치·삭제 필터·id 삭제를 기록하고, IN 필터 삭제는 메타데이터로 실제 적용한다
 * ({@code SimpleVectorStore} 는 1.0.1 에서 필터 삭제를 지원하지 않아 라이터 계약은 이 저장소로 검증한다).
 */
public final class RecordingVectorStore implements VectorStore {

	private final Map<String, Document> documents = new LinkedHashMap<>();
	private final List<List<Document>> addBatches = new ArrayList<>();
	private final List<Filter.Expression> deleteFilters = new ArrayList<>();
	private final List<List<String>> deletedIds = new ArrayList<>();

	@Override
	public void add(List<Document> docs) {
		addBatches.add(List.copyOf(docs));
		docs.forEach(document -> documents.put(document.getId(), document));
	}

	@Override
	public void delete(List<String> ids) {
		deletedIds.add(List.copyOf(ids));
		ids.forEach(documents::remove);
	}

	@Override
	public void delete(Filter.Expression expression) {
		deleteFilters.add(expression);
		if (expression.type() == Filter.ExpressionType.IN
				&& expression.left() instanceof Filter.Key key
				&& expression.right() instanceof Filter.Value value
				&& value.value() instanceof Collection<?> values) {
			documents.values().removeIf(document -> values.contains(document.getMetadata().get(key.key())));
			return;
		}
		throw new UnsupportedOperationException("테스트 저장소는 IN 필터 삭제만 지원합니다: " + expression);
	}

	@Override
	public List<Document> similaritySearch(SearchRequest request) {
		return List.of();
	}

	public Map<String, Document> documents() {
		return documents;
	}

	public List<List<Document>> addBatches() {
		return addBatches;
	}

	public List<Filter.Expression> deleteFilters() {
		return deleteFilters;
	}

	public List<List<String>> deletedIds() {
		return deletedIds;
	}

	public int calls() {
		return addBatches.size() + deleteFilters.size() + deletedIds.size();
	}
}
