<%--
  Created by IntelliJ IDEA.
  User: KJ
  Date: 2020/11/28
  Time: 16:35
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <h3>入门案例</h3>
    <a href="hello">入门案例</a><br>
    <a href="/voidTest">返回值之void</a><br>
    <a href="/modelandviewTest">返回值之modelandview</a>
    <a href="/paramsTest?name=zhangsan">参数绑定</a>

    <form action="/pojoTest" method="post">
        <input type="text" name="name" />
        <input type="text" name="age"/>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
