package org.egovframe.rte.ai.langchain4j.support;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * 결정적 가짜 임베딩 모델 — 텍스트의 코드포인트를 16개 버킷에 세어 정규화한다(같은 텍스트 → 같은 벡터, 서버 불필요). 호출 횟수를 기록한다.
 */
public final class FakeEmbeddingModel implements EmbeddingModel {

	public static final int DIMENSIONS = 16;

	private int calls;

	@Override
	public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
		calls++;
		List<Embedding> embeddings = new ArrayList<>(segments.size());
		for (TextSegment segment : segments) {
			embeddings.add(Embedding.from(vector(segment.text())));
		}
		return Response.from(embeddings);
	}

	@Override
	public int dimension() {
		return DIMENSIONS;
	}

	public int calls() {
		return calls;
	}

	public static float[] vector(String text) {
		float[] vector = new float[DIMENSIONS];
		if (text != null) {
			text.codePoints().forEach(cp -> vector[Math.floorMod(cp, DIMENSIONS)] += 1f);
		}
		double norm = 0;
		for (float v : vector) {
			norm += v * v;
		}
		if (norm > 0) {
			float scale = (float) (1.0 / Math.sqrt(norm));
			for (int i = 0; i < vector.length; i++) {
				vector[i] *= scale;
			}
		}
		return vector;
	}
}
