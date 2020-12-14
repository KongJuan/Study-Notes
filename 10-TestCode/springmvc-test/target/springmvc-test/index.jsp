<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<html>
<body>
<h3>入门案例</h3>
<a href="hello">入门案例</a><br>

<!-- request请求的内容类型主要分为：K/V类型、Multipart类型、JSON类型 -->
<!-- 将request请求参数，绑定到简单类型（基本类型和String类型）方法参数 -->
<!-- 直接绑定 -->
<a href="${pageContext.request.contextPath}/user/findUserById?id=1">查询用户1</a><br>
<!-- @RequestParam注解绑定 -->
<a href="${pageContext.request.contextPath}/user/findUserById2?uid=1">查询用户2</a>
</body>
</html>
