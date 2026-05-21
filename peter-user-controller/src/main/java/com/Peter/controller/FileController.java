package com.Peter.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.Peter.utils.BaseResultUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.transform.Result;
import java.io.OutputStream;
import java.net.URLEncoder;

@Slf4j
@RestController
@RequestMapping("/user/files")
public class FileController {
    //文件上传存储路径
    private static final  String filePath=System.getProperty("user.dir")+"/files/";

    @org.springframework.beans.factory.annotation.Value("${server.port:8489}")
    private String port;

    @org.springframework.beans.factory.annotation.Value("${ip:localhost}")
    private String ip;

    @Value("${env:prod}")
    private String env;
    /**
     * 文件上传
     */
    /**
     * todo 因为存储在本地环境，如果有大量文件需要存储，存在瓶颈
     * 文件上传之前，并未进行有限性校验，完整性校验
     *
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file){
        String flag;
        //todo 多个线程在1ms内肯获取相同的flag
        synchronized (FileController.class){
            flag=System.currentTimeMillis()+"";
            ThreadUtil.sleep(1L);
        }//同步代码块，防止多线程同时进来，获取时间戳
            String fileName=file.getOriginalFilename();
        try {
            if(!FileUtil.isDirectory(filePath)){
                FileUtil.mkdir(filePath);
            }
            //文件储存形式：时间戳-文件名
            FileUtil.writeBytes(file.getBytes(),filePath+flag+"-"+fileName);
            System.out.println(fileName+"--上传成功");
        }catch (Exception e){
            System.err.println(fileName+"--上传失败");
        }
        String http=null;
        if(env.equals("prod")){
            http="http://"+ip+":"+"api/user/files/";
        }else{
            http="http://"+ip+":"+port+"/user/files";
        }
        return (Result) BaseResultUtils.success(http+flag+"-"+fileName);
    }
/**
 *富文本文件上穿
 */
@PostMapping("/editor/upload")
    public Dict editorUpload(MultipartFile file){
    String flag;
    synchronized (FileController.class){
        flag=System.currentTimeMillis()+"";
        ThreadUtil.sleep(1L);
    }//同步块，防止并发
    String fileName=file.getOriginalFilename();
    try {
        if(!FileUtil.isDirectory(filePath)){
            FileUtil.mkdir(filePath);
        }
        //文件储存形式：时间戳-文件名
        FileUtil.writeBytes(file.getBytes(),filePath+flag+"-"+fileName);
        System.out.println(fileName+"--上传成功");

    }catch (Exception e){
        System.err.println(fileName+"--上传失败");
    }
    //创建文件的访问路径
    String http=null;
    if(env.equals("prod")){
        http="http://"+ip+":"+"api/user/files/";
    }else{
        http="http://"+ip+":"+port+"/user/files";
    }

    return Dict.create().set("errno",0).set("data",Dict.create().set("url",http+flag+"-"+fileName));
}
/**
 * 获取文件
 */
@GetMapping("/{flag}")
    public void avartarPath(@PathVariable String flag, HttpServletResponse response){
    OutputStream os;
    try {
        if(StrUtil.isNotEmpty(flag)){
            response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(flag,"UTF-8"));
            response.setContentType("application/octet-stream");
            byte[] bytes = FileUtil.readBytes(filePath+flag);
            os = response.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();
        }
    }catch (Exception e){
        System.out.println("文件下载失败");
    }
}
/**
 * 删除文件
 */
@DeleteMapping("/{flag}")
    public void delFile(@PathVariable String flag){
    FileUtil.del(filePath+flag);
    System.out.println("删除文件"+flag+"成功");
}

}
