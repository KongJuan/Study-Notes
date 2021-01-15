package com.kj.controller;

import com.kj.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.UUID;

@RequestMapping("user")
@Controller
public class UserController {

    @RequestMapping("findUserById")
    public String findUserById(Integer id, Model model, HttpServletRequest request){
        model.addAttribute("msg", "直接参数绑定接收到的参数："+id);
        //model.addAttribute("msg", "通过Request getParameter参数接收到的参 数："+request.getParameter("id"));
        return "success";
    }

    @RequestMapping("findUserById2")
    public String findUserById2(@RequestParam("uid") Integer id, Model model) {
        model.addAttribute("msg", "@RequestParam注解绑定："+id);
        return "success";
    }

    @RequestMapping("saveUser")
    public String saveUser(User user, Model model)
    {
        model.addAttribute("msg", "将request请求参数，绑定到POJO类型(简单POJO和包装POJO的)方法参数："+user.toString());
        return "success";
    }
    @RequestMapping("findUserByIds")
    public String findUserByIds(int[] id,Model model){
        model.addAttribute("msg", "将request请求参数，绑定到数组参数："+id[1]);
        return "success";
    }

    //报错
    //Request processing failed; nested exception is java.lang.IllegalStateException: No primary or default constructor found for interface java.util.List
    @RequestMapping("findUserByIds2")
    public String findUserByIds2(List<Integer> id,Model model){
        model.addAttribute("msg","将request请求参数，绑定到集合参数："+id);
        return "success";
    }

    @RequestMapping("findUserByIds3")
    public String findUserByIds3(User user,Model model){
        model.addAttribute("msg","将request请求参数，绑定到对象的List参数:"+user.getUid());
        return "success";
    }

    @RequestMapping("updateUser")
    public String updateUser(User user,Model model){
        model.addAttribute("msg","将request请求参数，绑定到[元素是POJO类型的List集合或Map集合]参数"+user);
        return "success";
    }

    @RequestMapping("deleteUser")
    public String deleteUser(User user,Model model){
        model.addAttribute("msg","请求参数是：【年月日】 格式 :"+user.getBirthday());
        return "success";
    }
    @RequestMapping("deleteUser2")
    public String deleteUser2(User user,Model model){
        model.addAttribute("msg","请求参数是：【年月日 时分秒】 格式:"+user.getBirthday());
        return "success";
    }

    @RequestMapping("fileupload")
    public String fileupload(MultipartFile uploadFile,Model model) throws Exception {
        // 编写文件上传逻辑（mvc模式和三层结构模式）
        // 三层模式：表现层（controller、action）、业务层（service、biz）、持久层（dao、 mapper）
        // MVC模式主要就是来解决表现层的问题的（原始的表现层是使用Servlet编写，即编写业务逻 辑，又编写视图展示）
        if (uploadFile != null)
        {
            System.out.println(uploadFile.getOriginalFilename());
            // 原始图片名称
            String originalFilename = uploadFile.getOriginalFilename();
            // 如果没有图片名称，则上传不成功
            if (originalFilename != null && originalFilename.length() > 0) {
                // 存放图片的物理路径
                String picPath = "G:\\";
                // 获取上传文件的扩展名
                String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
                // 新文件的名称
                String newFileName = UUID.randomUUID() + extName;
                // 新的文件
                File newFile = new File(picPath + newFileName);
                // 把上传的文件保存成一个新的文件
                uploadFile.transferTo(newFile);
                // 同时需要把新的文件名更新到数据库中
                }
        }
        model.addAttribute("msg","上传成功");
        return "success";
    }
}
