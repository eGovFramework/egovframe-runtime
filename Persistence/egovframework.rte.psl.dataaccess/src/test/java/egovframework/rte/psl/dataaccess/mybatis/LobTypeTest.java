package egovframework.rte.psl.dataaccess.mybatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import egovframework.rte.psl.dataaccess.TestBase;
import egovframework.rte.psl.dataaccess.dao.TypeTestMapper;
import egovframework.rte.psl.dataaccess.vo.LobTestVO;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 *   2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 *   2014.01.22 권윤정  SLF4J로 로깅방식 변경
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class LobTypeTest extends TestBase {

	@Resource(name = "typeTestMapper")
	TypeTestMapper typeTestMapper;

	@Before
	public void onSetUp() throws Exception {
		// 외부에 sql file 로부터 DB 초기화 (TypeTest 기존 테이블
		// 삭제/생성)
		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("META-INF/testdata/sample_schema_ddl_typetest_" + usingDBMS + ".sql"), true);
	}

	public LobTestVO makeVO() throws Exception {
		LobTestVO vo = new LobTestVO();
		vo.setId(1);

		ResourceLoader resourceLoader = new DefaultResourceLoader();
		org.springframework.core.io.Resource resource = resourceLoader.getResource("META-INF/testdata/iBATIS-SqlMaps-2_en.pdf");
		File file = resource.getFile();
		byte[] fileBArray = new byte[(int) file.length()];
		@SuppressWarnings("resource")
		FileInputStream fis = new FileInputStream(file);
		fis.read(fileBArray);
		vo.setBlobType(fileBArray);

		resource = resourceLoader.getResource("META-INF/testdata/index-all.html");
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append("\n");
		}
		vo.setClobType(builder.toString());

		return vo;
	}

	public void checkResult(LobTestVO vo, LobTestVO resultVO) {
		assertNotNull(resultVO);
		assertEquals(vo.getId(), resultVO.getId());

		// 바로 비교는 불가 - length 비교 및 첫 바이트, 끝 바이트가 동일한지 확인한다.
		int srcLength = vo.getBlobType().length;
		assertEquals(srcLength, resultVO.getBlobType().length);
		assertEquals(vo.getBlobType()[0], resultVO.getBlobType()[0]);
		assertEquals(vo.getBlobType()[srcLength - 1], resultVO.getBlobType()[srcLength - 1]);

		assertEquals(vo.getClobType(), resultVO.getClobType());
		LoggerFactory.getLogger(this.getClass()).debug(resultVO.getClobType());

	}

	@Test
	public void testLobTypeTest() throws Exception {

		LobTestVO vo = makeVO();

		// insert
		typeTestMapper.insert("insertLobTest", vo);

		// select
		LobTestVO resultVO = (LobTestVO) typeTestMapper.selectOne("selectLobTest", vo);

		// check
		checkResult(vo, resultVO);

	}

}
