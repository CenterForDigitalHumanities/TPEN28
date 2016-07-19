package edu.slu.util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		ServletContext context = session.getServletContext();
		context.setAttribute(session.getId(), session);
		//System.out.println("Adding session " + session.getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		ServletContext context = session.getServletContext();
		context.removeAttribute(session.getId());
		//System.out.println("Removing session " + session.getId());
	}

}
