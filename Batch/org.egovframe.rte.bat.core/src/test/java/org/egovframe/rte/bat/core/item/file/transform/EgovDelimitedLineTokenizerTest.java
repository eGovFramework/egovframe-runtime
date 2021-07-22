package org.egovframe.rte.bat.core.item.file.transform;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * EgovDelimitedLineTokenizer JUnit Test 클래스
 *
 * @author 원송연
 * @since 2021.07.22
 * @version 1.0
 *
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2021.07.22  원송연           최초 생성
 */
public class EgovDelimitedLineTokenizerTest {

	@Test(expected = IllegalStateException.class)
	public void testEgovDelimitedLineTokenizer() {
		// " 따옴표를 tokenizer 로 사용할경우 state exception 발생 확인
		EgovDelimitedLineTokenizer egovDelimitedLineTokenizer = new EgovDelimitedLineTokenizer(new String("\""));
	}

	@Test
	public void testDoTokenize() {
		EgovDelimitedLineTokenizer egovDelimitedLineTokenizer = new EgovDelimitedLineTokenizer(new String("?"));
		String text = "cat?tiger?dog";
		List<String> expected = Arrays.asList("cat", "tiger", "dog");
		List<String> actual = egovDelimitedLineTokenizer.doTokenize(text);
		String[] expectedArr = new String[3];
		String[] actualArr = new String[3];
		expected.toArray(expectedArr);
		actual.toArray(actualArr);

		// doTokenize 결과 확인
		assertArrayEquals(expectedArr, actualArr);
	}
}