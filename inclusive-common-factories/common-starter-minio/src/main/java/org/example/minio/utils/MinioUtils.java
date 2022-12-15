package org.example.minio.utils;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
* minio静态访问工具类
* @author wcx
* @date 2022/12/14
*/
@UtilityClass
public class MinioUtils {
    /**
    * bean初始化时赋值，限定为同包访问
    */
    MinioClient minioClient;

    /**
     * 创建一个桶
     */
    public void createBucket(String bucket) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    /**
     * 上传一个文件
     */
    public void uploadFile(InputStream stream, String bucket, String objectName) throws Exception {
        minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(objectName)
                .stream(stream, -1, 10485760).build());
    }

    /**
     * 列出所有的桶
     */
    public List<String> listBuckets() throws Exception {
        List<Bucket> list = minioClient.listBuckets();
        List<String> names = new ArrayList<>();
        list.forEach(b -> {
            names.add(b.name());
        });
        return names;
    }

    /**
     * 下载一个文件
     */
    public InputStream download(String bucket, String objectName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectName).build());
        return stream;
    }

    /**
     * 删除一个桶
     */
    public void deleteBucket(String bucket) throws Exception {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
    }

    /**
     * 删除一个对象
     */
    public void deleteObject(String bucket, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
    }

    /**
     * 获取文件信息
     */
    public String getObjectInfo(String bucket, String objectName) throws Exception {
        return minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build()).toString();
    }

    /**
     * 生成一个给HTTP GET请求用的presigned URL。浏览器/移动端的客户端可以用这个URL进行下载，即使其所在的存储桶是私有的。
     */
    public String getPresignedObjectUrl(String bucketName, String objectName, Integer expires) throws Exception {
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs
                .builder().bucket(bucketName).object(objectName).expiry(expires).method(Method.GET).build();
        return minioClient.getPresignedObjectUrl(build);
    }

    /**
     * 列出一个桶中的所有文件和目录
     */
    public List<Fileinfo> listFiles(String bucket) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucket).recursive(true).build());

        List<Fileinfo> infos = new ArrayList<>();
        results.forEach(r->{
            Fileinfo info = new Fileinfo();
            try {
                Item item = r.get();
                info.setFilename(item.objectName());
                info.setDirectory(item.isDir());
                infos.add(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return infos;
    }

    /**
     * 复制文件
     */
    public void copyObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject) throws Exception {
        MinioUtils.createBucket(targetBucket);
        minioClient.copyObject(CopyObjectArgs.builder().bucket(targetBucket).object(targetObject)
                .source(CopySource.builder().bucket(sourceBucket).object(sourceObject).build()).build());
    }

    /**
     * 获取minio中所有的文件
     */
    public List<Fileinfo> listAllFile() throws Exception {
        List<String> list = MinioUtils.listBuckets();
        List<Fileinfo> fileinfos = new ArrayList<>();
        for (String bucketName : list) {
            fileinfos.addAll(MinioUtils.listFiles(bucketName));
        }
        return fileinfos;
    }

    /**
    * 如果自定义MinioClient时，需要将MinioClient注入进来，防止工具类失效
    */
    public static void setMinoClient(MinioClient minoClient) {
        if (minioClient == null) {
            MinioUtils.minioClient = minoClient;
        }
        throw new RuntimeException("minioClient赋值失败，已经被赋值了,不能重复赋值");
    }
}
