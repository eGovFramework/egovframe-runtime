package org.egovframe.rte.fdl.excel.download;

import org.egovframe.rte.fdl.excel.vo.UsersVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CategoryPOIExcelController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryPOIExcelController.class);

	@RequestMapping("/sale/listPOIExcelCategory.do")
    public ModelAndView selectCategoryList() throws Exception {

        LOGGER.debug("### selectCategoryList start !!!");

        List<Map<String, ?>> lists = new ArrayList<Map<String, ?>>();

        Map<String, String> mapCategory = new HashMap<String, String>();
        mapCategory.put("id", "0000000001");
        mapCategory.put("name", "Sample Test");
        mapCategory.put("description", "This is initial test data.");
        mapCategory.put("useyn", "Y");
        mapCategory.put("reguser", "test");

        lists.add(mapCategory);
        LOGGER.debug("### selectCategoryList lists.add1");

        mapCategory.put("id", "0000000002");
        mapCategory.put("name", "test Name");
        mapCategory.put("description", "test Deso1111");
        mapCategory.put("useyn", "Y");
        mapCategory.put("reguser", "test");

        lists.add(mapCategory);
        LOGGER.debug("### selectCategoryList lists.add2");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("category", lists);

        return new ModelAndView("categoryPOIExcelView", "categoryMap", map);
    }

    @RequestMapping("/sale/listPOIExcelVOCategory.do")
    public ModelAndView selectCategoryVOList() throws Exception {

        LOGGER.debug("### selectCategoryVOList start !!!");

        List<UsersVO> lists = new ArrayList<UsersVO>();

        UsersVO users = new UsersVO();


        //Map<String, String> mapCategory = new HashMap<String, String>();
        users.setId("0000000001");
        users.setName("Sample Test");
        users.setDescription("This is initial test data.");
        users.setUseYn("Y");
        users.setRegUser("test");

        lists.add(users);
        LOGGER.debug("### selectCategoryVOList lists.add1");

        users.setId("0000000002");
        users.setName("test Name");
        users.setDescription("test Deso1111");
        users.setUseYn("Y");
        users.setRegUser("test");


        lists.add(users);
        LOGGER.debug("### selectCategoryVOList lists.add2");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("category", lists);

        return new ModelAndView("categoryPOIExcelView", "categoryMap", map);
    }
}
