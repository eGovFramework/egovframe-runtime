package org.egovframe.rte.ai.springai.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/**
 * 결정적 가짜 임베딩 모델 — 텍스트의 코드포인트를 16개 버킷에 세어 정규화한다(같은 텍스트 → 같은 벡터, 서버 불필요).
 */
public final class FakeEmbeddingModel implements EmbeddingModel {

	public static final int DIMENSIONS = 16;

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {
		List<Embedding> embeddings = new ArrayList<>();
		List<String> texts = request.getInstructions();
		for (int i = 0; i < texts.size(); i++) {
			embeddings.add(new Embedding(embed(texts.get(i)), i));
		}
		return new EmbeddingResponse(embeddings);
	}

	@Override
	public float[] embed(Document document) {
		return embed(document.getText());
	}

	@Override
	public float[] embed(String text) {
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

	@Override
	public int dimensions() {
		return DIMENSIONS;
	}
}
