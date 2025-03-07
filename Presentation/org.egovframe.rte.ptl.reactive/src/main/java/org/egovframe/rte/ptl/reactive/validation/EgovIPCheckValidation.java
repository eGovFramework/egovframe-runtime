package org.egovframe.rte.ptl.reactive.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EgovIPCheckValidation implements ConstraintValidator<EgovIPCheck, String>  {

    private static final String IPV4_REGEX =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final String IPV6_REGEX =
            "([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:)|" +
                    "([0-9a-fA-F]{1,4}:){1,7}:|" +
                    "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|" +
                    "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|" +
                    "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|" +
                    "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|" +
                    "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|" +
                    "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|" +
                    ":((:[0-9a-fA-F]{1,4}){1,7}|:)|" +
                    "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|" +
                    "::(ffff(:0{1,4}){0,1}:){0,1}" +
                    "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}" +
                    "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|" +
                    "([0-9a-fA-F]{1,4}:){1,4}:" +
                    "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}" +
                    "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])";

    private final Pattern ipv4Pattern = Pattern.compile(IPV4_REGEX);
    private final Pattern ipv6Pattern = Pattern.compile(IPV6_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return ipv4Pattern.matcher(value).matches() || ipv6Pattern.matcher(value).matches();
    }

}
