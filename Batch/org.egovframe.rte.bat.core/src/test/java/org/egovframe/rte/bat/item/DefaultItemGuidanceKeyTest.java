package org.egovframe.rte.bat.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * DefaultItemReader / DefaultItemWriter 가 설정 누락 시 던지는 안내 예외 테스트.
 *
 * <p>안내문이 알려준 Job 파라미터 키를 그대로 설정했을 때 설정이 실제로 끝나는지 확인한다.
 * 안내문에서 키를 뽑아 되먹이므로, 안내문과 실제로 읽는 키가 어긋나면 실패한다.</p>
 *
 * @author 기여자
 * @version 1.0
 * @since 2026.09.03
 */
public class DefaultItemGuidanceKeyTest {

    private static final String STEP_NAME = "step1";

    /** 안내문이 예시로 드는 VO 클래스. 실재하지 않으므로 되먹일 때 로드 가능한 클래스로 치환한다. */
    private static final String GUIDED_VO_TYPE = "aa.bb.TestVo";

    private static final Pattern GUIDED_PARAMETER =
            Pattern.compile("(" + Pattern.quote(STEP_NAME) + "\\.[\\w.]+)=(\\S*)");

    /** Job 파라미터로 Writer 설정을 시도하고, 실패하면 예외 메시지를 돌려준다. */
    private String configureWriter(Map<String, String> jobParameters) throws ClassNotFoundException {
        try {
            new DefaultItemWriter<Object>().beforeStep(stepExecution(jobParameters));
            return null;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    /** Job 파라미터로 Reader 설정을 시도하고, 실패하면 예외 메시지를 돌려준다. */
    private String configureReader(Map<String, String> jobParameters) {
        try {
            new DefaultItemReader<Object>().beforeStep(stepExecution(jobParameters));
            return null;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    private StepExecution stepExecution(Map<String, String> jobParameters) {
        JobParametersBuilder builder = new JobParametersBuilder();
        for (Map.Entry<String, String> entry : jobParameters.entrySet()) {
            builder.addString(entry.getKey(), entry.getValue());
        }
        return new JobExecution(1L, builder.toJobParameters()).createStepExecution(STEP_NAME);
    }

    /** 안내문에 적힌 'key=value' 를 그대로 Job 파라미터로 만든다. */
    private Map<String, String> guidedParameters(String message) {
        Map<String, String> jobParameters = new LinkedHashMap<String, String>();
        Matcher matcher = GUIDED_PARAMETER.matcher(message);
        while (matcher.find()) {
            String value = matcher.group(2);
            jobParameters.put(matcher.group(1), GUIDED_VO_TYPE.equals(value) ? Object.class.getName() : value);
        }
        return jobParameters;
    }

    @Test
    @DisplayName("Writer 리소스 타입 안내문의 키로 설정하면 같은 안내문이 다시 나오지 않는다")
    public void writerResourceTypeGuidance() throws ClassNotFoundException {
        String message = configureWriter(new LinkedHashMap<String, String>());
        assertNotNull(message, "리소스 타입을 빼면 안내 예외가 나야 한다");

        Map<String, String> guided = guidedParameters(message);
        assertFalse(guided.isEmpty(), "안내문에 Job 파라미터 키가 있어야 한다: " + message);

        Map<String, String> jobParameters = new LinkedHashMap<String, String>();
        for (String key : guided.keySet()) {
            jobParameters.put(key, "delimitedFile");
        }
        String repeated = configureWriter(jobParameters);
        assertFalse(repeated != null && repeated.contains("리소스 타입 종류"),
                "안내문이 알려준 키로 설정했는데 같은 안내문이 되풀이된다: " + repeated);
    }

    @Test
    @DisplayName("Writer delimitedFile 안내문의 키로 설정하면 설정이 끝난다")
    public void writerDelimitedFileGuidance() throws ClassNotFoundException {
        Map<String, String> jobParameters = new LinkedHashMap<String, String>();
        jobParameters.put(STEP_NAME + ".writer.resource.type", "delimitedFile");

        String message = configureWriter(jobParameters);
        assertNotNull(message, "필수 설정을 빼면 안내 예외가 나야 한다");

        jobParameters.putAll(guidedParameters(message));
        assertNull(configureWriter(jobParameters),
                "안내문대로 설정했는데도 실패한다:\n" + message);
    }

    @Test
    @DisplayName("Writer fixedLengthFile 안내문의 키로 설정하면 설정이 끝난다")
    public void writerFixedLengthFileGuidance() throws ClassNotFoundException {
        Map<String, String> jobParameters = new LinkedHashMap<String, String>();
        jobParameters.put(STEP_NAME + ".writer.resource.type", "fixedLengthFile");

        String message = configureWriter(jobParameters);
        assertNotNull(message, "필수 설정을 빼면 안내 예외가 나야 한다");

        jobParameters.putAll(guidedParameters(message));
        assertNull(configureWriter(jobParameters),
                "안내문대로 설정했는데도 실패한다:\n" + message);
    }

    @Test
    @DisplayName("Writer jdbcDb 안내문의 키로 설정하면 설정이 끝난다")
    public void writerJdbcDbGuidance() throws ClassNotFoundException {
        Map<String, String> jobParameters = new LinkedHashMap<String, String>();
        jobParameters.put(STEP_NAME + ".writer.resource.type", "jdbcDb");

        String message = configureWriter(jobParameters);
        assertNotNull(message, "필수 설정을 빼면 안내 예외가 나야 한다");

        jobParameters.putAll(guidedParameters(message));
        assertNull(configureWriter(jobParameters),
                "안내문대로 설정했는데도 실패한다:\n" + message);
    }

    @Test
    @DisplayName("Reader jdbcDb 안내문의 키로 설정하면 설정이 끝난다")
    public void readerJdbcDbGuidance() {
        Map<String, String> jobParameters = new LinkedHashMap<String, String>();
        jobParameters.put(STEP_NAME + ".reader.resource.type", "jdbcDb");

        String message = configureReader(jobParameters);
        assertNotNull(message, "필수 설정을 빼면 안내 예외가 나야 한다");

        jobParameters.putAll(guidedParameters(message));
        assertNull(configureReader(jobParameters),
                "안내문대로 설정했는데도 실패한다:\n" + message);
    }

    @Test
    @DisplayName("Reader delimitedFile 안내문의 키로 설정하면 설정이 끝난다")
    public void readerDelimitedFileGuidance() {
        Map<String, String> jobParameters = new LinkedHashMap<String, String>();
        jobParameters.put(STEP_NAME + ".reader.resource.type", "delimitedFile");

        String message = configureReader(jobParameters);
        assertNotNull(message, "필수 설정을 빼면 안내 예외가 나야 한다");

        jobParameters.putAll(guidedParameters(message));
        assertNull(configureReader(jobParameters),
                "안내문대로 설정했는데도 실패한다:\n" + message);
    }
}
