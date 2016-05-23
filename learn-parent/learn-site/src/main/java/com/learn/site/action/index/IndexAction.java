/****/
package com.learn.site.action.index;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author liuwp
 * @date 2016年5月11日
 */
@Controller
@RequestMapping("index")
public class IndexAction {
	@RequestMapping("/index.shtml")
	public ModelAndView index() {
		return new ModelAndView("index");
	}
}
