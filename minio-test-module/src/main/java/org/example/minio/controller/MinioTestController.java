package org.example.minio.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.example.minio.utils.MinioUtils;
import org.example.core.result.R;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

@RestController
@RequestMapping("/minio")
public class MinioTestController {

    static final String bucket = "minio-test-bucket";

    /**
    * 上传文件
    */
    @PostMapping
    public R test(@RequestParam(value = "file") MultipartFile file) throws Exception {
        MinioUtils.uploadFile(file.getInputStream(),bucket,file.getOriginalFilename());
        return R.ok();
    }

    /**
    * 下载文件
    */
    @GetMapping
    public void get(HttpServletResponse response, String fileName) throws Exception {
        try (InputStream inputStream = MinioUtils.download(bucket,fileName); OutputStream outputStream = response.getOutputStream()){
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            IOUtils.copy(inputStream,outputStream);
        }
    }

    /**
    * 删除文件
    */
    @DeleteMapping
    public R delete(String fileName) throws Exception {
        MinioUtils.deleteObject(bucket,fileName);
        return R.ok();
    }

}
