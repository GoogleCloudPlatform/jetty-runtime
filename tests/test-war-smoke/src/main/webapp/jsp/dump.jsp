<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.Enumeration" contentType="text/plain"%>
request-uri=<%= request.getRequestURI() %>
servlet-path=<%= request.getServletPath() %>
path-info=<%= request.getPathInfo() %>
<%
    Enumeration e =request.getParameterNames();
    while(e.hasMoreElements())
    {
        String name = (String)e.nextElement();
%>
param-<%= name %>=<%= request.getParameter(name) %>
<%  }  %>

<c:forEach var="i" begin="1" end="1" step="1">
jstl-example=<c:out value="${i}"/>
</c:forEach>
