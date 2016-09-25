<%@ page import="a.org.fakereplace.integration.tomcat.clientproxy.UsedClass" %>
<html>
<body>
    index.jsp: <%= new UsedClass().getClassName() %>
</body>
</html>