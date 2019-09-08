package cn.neteast.manager.controller;

import cn.neteast.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
public class UploadController {

    //properties文件中包含此字符串, 故使用Value及SPEL表达式注入即可
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result update(MultipartFile file) {
        if (file != null) {
            //获取文件的扩展名
            String originalFilename = file.getOriginalFilename();
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            try {
                //创建一个客户端
                FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
                //执行文件上传
                String path = fastDFSClient.uploadFile(file.getBytes(), extName);
                //4、拼接返回的 url 和 ip 地址，拼装成完整的 url
                String url = FILE_SERVER_URL + path;
                System.out.println("url: " + url);
                return new Result(true, url);
            } catch (Exception e) {
                e.printStackTrace();
                return new Result(false, "上传失败");
            }
        } else {
            return new Result(false, "未选择上传文件");
        }

    }
}
