package com.lwl.social_media_platform.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Component
public class AliOSSUtils {
    private final String endpoint = "https://oss-cn-beijing.aliyuncs.com";
    private final String accessKeyId = "LTAI5tRPsN1A47mXCPw99ggu";
    private final String accessKeySecret = "6qgSi8D719ySK99miAo6yGwYCdOwgO";
    private final String bucketName = "homework1015";
    //上传图片
    public String upload(MultipartFile multipartFile) throws IOException{
        //获取上传文件输入流
        InputStream inputStream = multipartFile.getInputStream();
        //避免文件覆盖
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))+multipartFile.getOriginalFilename();
        //上传到OSS
        OSS oss = new OSSClientBuilder().build(endpoint,accessKeyId,accessKeySecret);
        oss.putObject(bucketName,fileName,inputStream);
        //文件访问路径
        String url = endpoint.split("//")[0] + "//" + bucketName + "." +endpoint.split("//")[1] + "/" + fileName;
        //关闭OSS
        oss.shutdown();
        return url;
    }
}
