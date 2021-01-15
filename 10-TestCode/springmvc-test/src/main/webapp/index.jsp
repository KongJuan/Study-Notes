<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<html>
    <body>
        <h3>入门案例</h3>
        <a href="hello">入门案例</a><br>
        <!-- request请求的内容类型主要分为：K/V类型、Multipart类型、JSON类型 -->
        <!-- 将request请求参数，绑定到简单类型（基本类型和String类型）方法参数 -->

        <!-- 1、直接绑定 -->
        <a href="${pageContext.request.contextPath}/user/findUserById?id=1">查询用户1</a><br>

        <!-- 2、@RequestParam注解绑定 -->
        <a href="${pageContext.request.contextPath}/user/findUserById2?uid=1">查询用户2</a><br>

        <!-- 3、将request请求参数，绑定到POJO类型(简单POJO和包装POJO的)方法参数 -->
        <form action="${pageContext.request.contextPath}/user/saveUser" method="post">
            用户名称：<input type="text" name="username"><br />
            用户性别：<input type="text" name="sex"><br />
            所属省份：<input type="text" name="address.provinceName"><br />
            所属城市：<input type="text" name="address.cityName"><br />
            <input type="submit" value="保存">
        </form>

        <!-- 将request请求参数，绑定到[元素是简单类型的集合或数组]参数 -->
        <!-- 使用数组接收 -->
        <a href="${pageContext.request.contextPath}/user/findUserByIds/?id=1&id=3">根据ID批量删除用户</a>
        <!--使用List接收（错误示例）
            Request processing failed; nested exception is java.lang.IllegalStateException: No primary or default constructor found for interface java.util.List-->
        <a href="${pageContext.request.contextPath}/user/findUserByIds2?id=1&id=3">根据ID批量删除用户</a>
        <!--使用Bean的List接收-->
        <a href="${pageContext.request.contextPath}/user/findUserByIds3?uid=1&uid=3">根据ID批量删除用户</a>

        <!-- 将request请求参数，绑定到[元素是POJO类型的List集合或Map集合]参数 -->
        <form action="${pageContext.request.contextPath}/user/updateUser" method="post">
            用户名称：<input type="text" name="username"><br />
            用户性别：<input type="text" name="sex"><br />
            <!-- itemList[集合下标]：集合下标必须从0开始 -->
            <!-- 辅助理解：先将name属性封装到一个Item对象中，再将该Item对象放入itemList集合的 指定下标处 -->
            购买商品1名称：<input type="text" name="itemList[0].name"><br />
            购买商品1价格：<input type="text" name="itemList[0].price"><br />
            购买商品2名称：<input type="text" name="itemList[1].name"><br />
            购买商品2价格：<input type="text" name="itemList[1].price"><br />
            <!-- itemMap['item3']：其中的item3、item4就是Map集合的key -->
            <!-- 辅助理解：先将name属性封装到一个Item对象中，再将该Item对象作为value放入 itemMap集合的指定key处 -->
            购买商品3名称：<input type="text" name="itemMap['item3'].name"><br />
            购买商品3价格：<input type="text" name="itemMap['item3'].price"><br />
            购买商品4名称：<input type="text" name="itemMap['item4'].name"><br />
            购买商品4价格：<input type="text" name="itemMap['item4'].price"><br />
            <input type="submit" value="保存">
        </form>

        <!-- 将request请求参数，绑定到Date类型方法参数 -->
        <!-- 请求参数是：【年月日】 格式 -->
        <a href="${pageContext.request.contextPath}/user/deleteUser?birthday=2018- 01-01">根据日期删除用户(String)</a>
        <!-- 请求参数是：【年月日 时分秒】 格式 -->
        <a href="${pageContext.request.contextPath}/user/deleteUser2?birthday=2018- 01-01 12:10:33">根据日期删除用户(Date)</a>
        <!-- 文件类型参数绑定 -->
        <form action="${pageContext.request.contextPath}/user/fileupload" method="post" enctype="multipart/form-data">
            图片：<input type="file" name="uploadFile" /><br />
            <input type="submit" value="上传" />
        </form>
    </body>
</html>
