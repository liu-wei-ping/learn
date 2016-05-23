package com.learn.site.common.filters;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.learn.site.common.utils.Constants;


public class InitUrlPathFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(final ServletRequest servletRequest,
			final ServletResponse servletResponse, final FilterChain chain)
			throws IOException, ServletException {
		final HttpServletResponse response = (HttpServletResponse) servletResponse;
		final HttpServletRequest request = (HttpServletRequest) servletRequest;
		final String contextPath = request.getContextPath().toString();
		response.sendRedirect(contextPath + Constants.LOGIN_URL);
		return;

	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

}
