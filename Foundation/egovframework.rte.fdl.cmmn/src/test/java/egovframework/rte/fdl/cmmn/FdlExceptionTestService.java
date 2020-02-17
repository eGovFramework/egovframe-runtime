package egovframework.rte.fdl.cmmn;

import java.util.Locale;

import egovframework.rte.fdl.cmmn.exception.FdlException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

public class FdlExceptionTestService implements ApplicationContextAware {
	private MessageSource messageSource;

	public void setApplicationContext(ApplicationContext applicatonContext) throws BeansException {
		ApplicationContext context = applicatonContext;
		this.messageSource = (MessageSource) context.getBean("messageSource");
	}

	public void testFdlException(int arg) throws FdlException {

		Locale.setDefault(Locale.ENGLISH);

		switch (arg) {

			case 1:
				throw new FdlException();

			case 2:
				throw new FdlException("message");

			case 3:
				throw new FdlException("message", new Exception("TEST FdlException"));

			case 4:
				throw new FdlException("message {0}", new Object[] { 1 }, new Exception("TEST FdlException"));

			case 5:
				throw new FdlException(messageSource, "error.fdl.msg1");

			case 6:
				throw new FdlException(messageSource, "error.fdl.msg2", new Exception("TEST FdlException"));

			case 7:
				throw new FdlException(messageSource,
				                       "error.fdl.msg3",
				                       Locale.KOREAN,
				                       new Exception("TEST FdlException"));

			case 8:
				throw new FdlException(messageSource,
				                       "error.fdl.msg4",
				                       new Object[] { "파라미터", "추가" },
				                       Locale.KOREAN,
				                       new Exception("TEST FdlException"));

			case 9:
				throw new FdlException(messageSource,
				                       "error.fdl.msg5",
				                       new Object[] { "parameter" },
				                       new Exception("TEST FdlException"));

			case 10:
				throw new FdlException(messageSource,
				                       "error.fdl.msg66",
				                       new Object[] { "parameter" },
				                       "default message",
				                       new Exception("TEST FdlException"));

			case 11:
				throw new FdlException(messageSource,
				                       "error.fdl.msg6",
				                       new Object[] { "parameter" },
				                       "default message",
				                       new Exception("TEST FdlException"));

			case 12:
				throw new FdlException(messageSource,
				                       "error.fdl.msg7",
				                       new Object[] { "파라미터" },
				                       "default message",
				                       Locale.KOREAN,
				                       new Exception("TEST FdlException"));

		}

	}

}
