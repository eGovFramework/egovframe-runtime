package org.egovframe.rte.fdl.security.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CategoryController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

	@RequestMapping("/sale/listCategory.do")
	public void selectCategoryList() throws Exception {
		LOGGER.debug("##### selectCategoryList #####");
		//return new ArrayList();
	}

	@RequestMapping("/sale/addCategoryView.do")
	public String addCategoryView() throws Exception {
		return "/sale/registerCategory";
	}

	@RequestMapping("/sale/addCategory.do")
	public String addCategory() throws Exception {
		return "forward:/sale/listCategory.do";
	}

	@RequestMapping("/sale/updateCategoryView.do")
	public String updateCategoryView() throws Exception {
		return "/sale/registerCategory";
	}

	@RequestMapping("/sale/updateCategory.do")
	public String updateCategory() throws Exception {
		return "forward:/sale/listCategory.do";
	}

	@RequestMapping("/sale/deleteCategory.do")
	public String deleteCategory() throws Exception {
		return "forward:/sale/listCategory.do";
	}

	@RequestMapping("/system/accessDenied.do")
	public String accessDenyView() throws Exception {
		LOGGER.debug("##### ACCESS DENY #####");
		return "/system/accessDenied";
	}

}
