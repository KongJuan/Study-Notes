package com.kyn.conf;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;

@Configuration
public class MyDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")  //将DataSource中属性与配置文件进行绑定
    public DataSource dataSource() throws SQLException {
        DruidDataSource druidDataSource=new DruidDataSource();
        //配置SQL监控功能
        druidDataSource.setFilters("stat");
        return druidDataSource;
    }

    //druid监控页
    @Bean
    public ServletRegistrationBean statViewServlet(){
        StatViewServlet statViewServlet=new StatViewServlet();
        ServletRegistrationBean<StatViewServlet> registrationBean=new ServletRegistrationBean<>(statViewServlet,"/druid/*");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean webStatFilter(){
        WebStatFilter webStatFilter=new WebStatFilter();
        FilterRegistrationBean<WebStatFilter> registrationBean=new FilterRegistrationBean<WebStatFilter>(webStatFilter);
        registrationBean.setUrlPatterns(Arrays.asList("/*"));
        registrationBean.addInitParameter("exclusions","*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return registrationBean;
    }

    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        // paginationInterceptor.setOverflow(false);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        // paginationInterceptor.setLimit(500);
        // 开启 count 的 join 优化,只针对部分 left join

        PaginationInnerInterceptor paginationInnerInterceptor=new PaginationInnerInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(paginationInnerInterceptor);
        return mybatisPlusInterceptor;
    }
}
