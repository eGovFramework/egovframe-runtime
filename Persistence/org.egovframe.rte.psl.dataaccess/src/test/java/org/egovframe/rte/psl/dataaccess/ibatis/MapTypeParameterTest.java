package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.MapTypeDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 * 2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataAccessTestConfig.class)
@Transactional
public class MapTypeParameterTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "mapTypeDAO")
    private MapTypeDAO mapTypeDAO;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    public Map<String, Object> makeMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("deptNo", new BigDecimal(90));
        map.put("deptName", "test Dept");
        map.put("loc", "test Loc");
        return map;
    }

    public void checkResult(Map<String, Object> map, Map<String, Object> resultMap) {
        assertNotNull(resultMap);
        assertEquals(map.get("deptNo"), resultMap.get("DEPTNO"));
        assertEquals(map.get("deptName"), resultMap.get("DEPTNAME"));
        assertEquals(map.get("loc"), resultMap.get("LOC"));
    }

    @Rollback(false)
    @Test
    public void testMapTypeInsert() {
        Map<String, Object> map = makeMap();

        // insert
        mapTypeDAO.insertDept("insertDeptUsingMap", map);

        // select
        Map<String, Object> resultMap = mapTypeDAO.selectDept("selectDeptUsingMap", map);

        // check
        checkResult(map, resultMap);
    }

    @Rollback(false)
    @Test
    public void testMapTypeUpdate() {
        Map<String, Object> map = makeMap();

        // insert
        mapTypeDAO.insertDept("insertDeptUsingMap", map);

        // data change
        map.put("deptName", "upd Dept");
        map.put("loc", "upd loc");

        // update
        int effectedRows = mapTypeDAO.updateDept("updateDeptUsingMap", map);
        assertEquals(1, effectedRows);

        // select
        Map<String, Object> resultMap = mapTypeDAO.selectDept("selectDeptUsingMap", map);

        // check
        checkResult(map, resultMap);
    }

    @Rollback(false)
    @Test
    public void testMapTypeDelete() {
        Map<String, Object> map = makeMap();

        // insert
        mapTypeDAO.insertDept("insertDeptUsingMap", map);

        // delete
        int effectedRows = mapTypeDAO.deleteDept("deleteDeptUsingMap", map);
        assertEquals(1, effectedRows);

        // select
        Map<String, Object> resultMap = mapTypeDAO.selectDept("selectDeptUsingMap", map);

        // check
        assertNull(resultMap);
    }

    @Rollback(false)
    @Test
    public void testEgovMapTest() {
        Map<String, Object> map = makeMap();

        // insert
        mapTypeDAO.insertDept("insertDeptUsingMap", map);

        // select
        Map<String, Object> resultMap = mapTypeDAO.selectDept("selectDeptUsingEgovMap", map);

        // check
        assertNotNull(resultMap);
        assertInstanceOf(ListOrderedMap.class, resultMap);
        assertEquals(map.get("deptNo"), resultMap.get("deptNo"));
        assertEquals(map.get("deptName"), resultMap.get("deptName"));
        assertEquals(map.get("loc"), resultMap.get("loc"));
    }

}
