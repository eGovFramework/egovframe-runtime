package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.TypeTestMapper;
import org.egovframe.rte.psl.dataaccess.vo.LobTestVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 * 2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 * 2014.01.22 권윤정  SLF4J로 로깅방식 변경
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataAccessTestConfig.class)
@Transactional
public class LobTypeTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "typeTestMapper")
    private TypeTestMapper typeTestMapper;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    public LobTestVO makeVO() throws IOException {
        LobTestVO vo = new LobTestVO();
        vo.setId(1);

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        org.springframework.core.io.Resource resource = resourceLoader.getResource("/META-INF/spring/iBATIS-SqlMaps-2_en.pdf");

        File file = resource.getFile();
        byte[] fileBArray = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(fileBArray);
        vo.setBlobType(fileBArray);

        resource = resourceLoader.getResource("/META-INF/spring/index-all.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
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
        int srcLength = vo.getBlobType().length;
        assertEquals(vo.getBlobType().length, resultVO.getBlobType().length);
        assertEquals(vo.getBlobType()[0], resultVO.getBlobType()[0]);
        assertEquals(vo.getBlobType()[srcLength - 1], resultVO.getBlobType()[srcLength - 1]);
        assertEquals(vo.getClobType(), resultVO.getClobType());

        LoggerFactory.getLogger(this.getClass()).debug(resultVO.getClobType());
    }

    @Rollback(false)
    @Test
    public void testLobTypeTest() throws IOException {
        LobTestVO vo = makeVO();

        // insert
        typeTestMapper.insert("insertLobTest", vo);

        // select
        LobTestVO resultVO = typeTestMapper.selectOne("selectLobTest", vo);

        // check
        checkResult(vo, resultVO);
    }

}
