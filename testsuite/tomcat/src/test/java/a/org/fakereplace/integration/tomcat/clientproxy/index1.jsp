<%@ page import="a.org.fakereplace.integration.tomcat.clientproxy.UsedClass" %>
<html>
<body>
    index1.jsp: <%= new UsedClass().getClassName() %>
</body>
</html>
