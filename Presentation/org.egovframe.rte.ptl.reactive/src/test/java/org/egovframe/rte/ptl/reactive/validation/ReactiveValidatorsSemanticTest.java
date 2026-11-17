package org.egovframe.rte.ptl.reactive.validation;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReactiveValidatorsSemanticTest {

    @Test
    void crnCheck_validatesBusinessRegistrationNumberChecksum() {
        EgovCrnCheckValidation validator = new EgovCrnCheckValidation();

        // 가중치는 앞 9자리에만 적용된다(국세청 규격).
        assertTrue(validator.isValid("124-81-00998", null), "사업자등록번호는 유효한 체크디지트를 true로 판정해야 한다");
        assertTrue(validator.isValid("220-81-62517", null), "사업자등록번호는 유효한 체크디지트를 true로 판정해야 한다");
        assertTrue(validator.isValid("101-81-16293", null), "사업자등록번호는 유효한 체크디지트를 true로 판정해야 한다");
        assertTrue(validator.isValid("1248100998", null), "사업자등록번호는 하이픈 없는 유효 번호를 true로 판정해야 한다");

        assertFalse(validator.isValid("124-81-00990", null), "사업자등록번호는 체크디지트 오류를 false로 판정해야 한다");
        assertFalse(validator.isValid("220-81-62510", null), "사업자등록번호는 체크디지트 오류를 false로 판정해야 한다");
        assertFalse(validator.isValid("124-81-0099", null), "사업자등록번호는 9자리 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("124-81-009989", null), "사업자등록번호는 11자리 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("12481abcde", null), "사업자등록번호는 숫자가 아닌 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("", null), "사업자등록번호는 빈 문자열을 false로 판정해야 한다");
        assertFalse(validator.isValid("1248100998\n", null), "사업자등록번호는 뒤에 개행이 붙은 입력을 예외 없이 false로 판정해야 한다");
        assertFalse(validator.isValid("1248100998\r\n", null), "사업자등록번호는 뒤에 CRLF가 붙은 입력을 예외 없이 false로 판정해야 한다");
    }

    @Test
    void cnCheck_validatesCorporateRegistrationNumberChecksum() {
        EgovCnCheckValidation validator = new EgovCnCheckValidation();

        assertTrue(validator.isValid("110111-0000002", null), "법인등록번호는 유효한 체크디지트를 true로 판정해야 한다");
        assertTrue(validator.isValid("134211-0123458", null), "법인등록번호는 유효한 체크디지트를 true로 판정해야 한다");

        assertFalse(validator.isValid("110111-0000000", null), "법인등록번호는 체크디지트 오류를 false로 판정해야 한다");
        assertFalse(validator.isValid("134211-0123450", null), "법인등록번호는 체크디지트 오류를 false로 판정해야 한다");
        assertFalse(validator.isValid("110111-000000", null), "법인등록번호는 12자리 입력을 false로 판정해야 한다");
    }

    @Test
    void rrnCheck_validatesResidentRegistrationNumberChecksum() {
        EgovRrnCheckValidation validator = new EgovRrnCheckValidation();

        // 실제 식별번호 아닌 합성값 (존재할 수 없는 날짜 + 유효 체크섬, 검증기는 패턴·체크섬만 검증)
        assertTrue(validator.isValid("991332-1123459", null), "주민등록번호 검증기는 날짜를 검증하지 않고 체크섬만 검증하므로 존재할 수 없는 날짜라도 true로 판정한다");
        assertTrue(validator.isValid("001332-2123452", null), "주민등록번호 검증기는 날짜를 검증하지 않고 체크섬만 검증하므로 존재할 수 없는 날짜라도 true로 판정한다");

        assertFalse(validator.isValid("991332-1123450", null), "주민등록번호는 체크디지트 오류를 false로 판정해야 한다");
        assertFalse(validator.isValid("991332-5123459", null), "주민등록번호는 성별코드 패턴 위반을 false로 판정해야 한다");
        assertFalse(validator.isValid("991332-112345", null), "주민등록번호는 12자리 입력을 false로 판정해야 한다");
    }

    @Test
    void mobilePhoneCheck_validatesMobilePhonePattern() {
        EgovMobilePhoneCheckValidation validator = new EgovMobilePhoneCheckValidation();

        // 하이픈 선제거 : 국번·자리수만 판정
        assertTrue(validator.isValid("010-1234-5678", null), "휴대전화번호는 010 국번의 유효 번호를 true로 판정해야 한다");
        assertTrue(validator.isValid("011-123-4567", null), "휴대전화번호는 011 국번의 유효 번호를 true로 판정해야 한다");
        assertTrue(validator.isValid("016-1234-5678", null), "휴대전화번호는 016 국번의 유효 번호를 true로 판정해야 한다");

        assertFalse(validator.isValid("012-1234-5678", null), "휴대전화번호는 국번 규칙 위반을 false로 판정해야 한다");
        assertFalse(validator.isValid("02-1234-5678", null), "휴대전화번호는 일반전화 형식을 false로 판정해야 한다");
        assertFalse(validator.isValid("010-1234-56789", null), "휴대전화번호는 12자리 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("010-123-456", null), "휴대전화번호는 9자리 입력을 false로 판정해야 한다");
    }

    @Test
    void phoneCheck_validatesPhonePattern() {
        EgovPhoneCheckValidation validator = new EgovPhoneCheckValidation();

        assertTrue(validator.isValid("02-1234-5678", null), "일반전화번호는 02 지역번호의 유효 번호를 true로 판정해야 한다");
        assertTrue(validator.isValid("031-123-4567", null), "일반전화번호는 031 지역번호의 유효 번호를 true로 판정해야 한다");

        assertFalse(validator.isValid("1234", null), "일반전화번호는 너무 짧은 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("02-1234-56789012", null), "일반전화번호는 너무 긴 입력을 false로 판정해야 한다");
    }

    @Test
    void englishCheck_validatesEnglishOnlyPattern() {
        EgovEnglishCheckValidation validator = new EgovEnglishCheckValidation();

        assertTrue(validator.isValid("abc", null), "영문 검증기는 소문자 영문만 있는 입력을 true로 판정해야 한다");
        assertTrue(validator.isValid("ABCdef", null), "영문 검증기는 대소문자 영문 입력을 true로 판정해야 한다");
        // 빈 문자열 허용 (필수값 여부는 @EgovNullCheck 담당)
        assertTrue(validator.isValid("", null), "영문 검증기는 빈 문자열을 true로 판정해야 한다");

        assertFalse(validator.isValid("한글", null), "영문 검증기는 한글 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("abc123", null), "영문 검증기는 숫자가 포함된 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("abc def", null), "영문 검증기는 공백이 포함된 입력을 false로 판정해야 한다");
    }

    @Test
    void koreanCheck_validatesKoreanOnlyPattern() {
        EgovKoreanCheckValidation validator = new EgovKoreanCheckValidation();

        assertTrue(validator.isValid("한글", null), "한글 검증기는 완성형 한글 입력을 true로 판정해야 한다");
        assertTrue(validator.isValid("ㄱㄴㄷ", null), "한글 검증기는 한글 자음 입력을 true로 판정해야 한다");

        assertFalse(validator.isValid("abc", null), "한글 검증기는 영문 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("한글abc", null), "한글 검증기는 한글과 영문이 섞인 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("한 글", null), "한글 검증기는 공백이 포함된 입력을 false로 판정해야 한다");
    }

    @Test
    void pwdCheck_validatesPasswordRules() {
        EgovPwdCheckValidation validator = new EgovPwdCheckValidation();

        assertTrue(validator.isValid("Egov!2xk", null), "비밀번호는 길이와 문자 조합 규칙을 만족하면 true로 판정해야 한다");
        assertTrue(validator.isValid("Pa$5wOrd9", null), "비밀번호는 길이와 문자 조합 규칙을 만족하면 true로 판정해야 한다");

        assertFalse(validator.isValid("Egov!2x", null), "비밀번호는 8자 미만 입력을 false로 판정해야 한다");
        assertFalse(validator.isValid("Egovxxkq", null), "비밀번호는 숫자와 특수문자가 없으면 false로 판정해야 한다");
        assertFalse(validator.isValid("Egov!2aaa", null), "비밀번호는 동일문자 3회 반복을 false로 판정해야 한다");
        assertFalse(validator.isValid("Egov!2abc", null), "비밀번호는 오름차순 연속 3자를 false로 판정해야 한다");
        assertFalse(validator.isValid("Egov!2123", null), "비밀번호는 연속 숫자 3자를 false로 판정해야 한다");
    }

    @Test
    void pwdCheck_consecutiveDetectionIsLocaleIndependent() {
        // 터키어 로케일 : 'i' → 'İ'(U+0130) 변환으로 'ghi'·'hij' 연속열 미탐지 우회 (Locale.ROOT 고정으로 방지)
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(EgovPwdCheckValidation.consecutivePasswordCheck("ghi"),
                    "터키어 로케일에서도 'ghi' 오름차순 연속 3자는 탐지되어야 한다");

            EgovPwdCheckValidation validator = new EgovPwdCheckValidation();
            boolean turkish = validator.isValid("Egov!2ghi", null);
            Locale.setDefault(Locale.ENGLISH);
            boolean english = validator.isValid("Egov!2ghi", null);
            assertFalse(turkish, "터키어 로케일에서 'ghi' 연속을 포함한 비밀번호는 거부되어야 한다");
            assertEquals(english, turkish, "연속 문자 검증 결과는 JVM 기본 로케일과 무관해야 한다");
        } finally {
            Locale.setDefault(original);
        }
    }
}
