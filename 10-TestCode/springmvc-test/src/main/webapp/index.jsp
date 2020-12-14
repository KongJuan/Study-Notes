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
<a href="${pageContext.request.contextPath}/user/findUserById2?uid=1">查询用户2</a><br>
<!-- 将request请求参数，绑定到POJO类型(简单POJO和包装POJO的)方法参数 -->
<form action="${pageContext.request.contextPath}/user/saveUser" method="post">
    用户名称：<input type="text" name="username"><br />
    用户性别：<input type="text" name="sex"><br />
    所属省份：<input type="text" name="address.provinceName"><br />
    所属城市：<input type="text" name="address.cityName"><br />
    <input type="submit" value="保存">
</form>
</body>
</html>
