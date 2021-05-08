package cn.com.caeri.pan.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @program: rohs_web
 * @description: zip文件处理util
 * @author: liy
 * @create: 2021-01-07 17:42
 **/
public class ZipUtil {

    /**
     * 压缩文件 支持单个文件或者文件目录
     *
     * @param filePath 待压缩的文件路径
     * @return 压缩后的文件
     */
    public static File zip(String filePath) throws IOException {
        File target = null;
        File source = new File(filePath);
        if (source.exists()) {
            // 压缩文件名=源文件名.zip
            String zipName = source.getName() + ".zip";
            target = new File(source.getParent(), zipName);
            if (target.exists()) {
                target.delete();
                // 删除旧的文件
            }
            FileOutputStream fos = null;
            ZipOutputStream zos = null;
            try {
                fos = new FileOutputStream(target);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                // 添加对应的文件Entry
                addEntry("/", source, zos);
            }finally {
                close(zos, fos);
            }
        }
        return target;
    }
    /**
     * 扫描添加文件Entry
     *
     * @param base
     *      基路径
     *
     * @param source
     *      源文件
     * @param zos
     *      Zip文件输出流
     * @throws IOException
     */
    private static void addEntry(String base, File source, ZipOutputStream zos) throws IOException {
        // 按目录分级，形如：/aaa/bbb.txt
        String entry = base + source.getName();
        if (source.isDirectory()) {
            for (File file : source.listFiles()) {
                // 递归列出目录下的所有文件，添加文件Entry
                addEntry(entry + "/", file, zos);
            }
        } else {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                byte[] buffer = new byte[1024 * 10];
                fis = new FileInputStream(source);
                bis = new BufferedInputStream(fis, buffer.length);
                int read = 0;
                zos.putNextEntry(new ZipEntry(entry));
                while ((read = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, read);
                }
                zos.closeEntry();
            }
            finally {
                close(bis, fis);
            }
        }
    }


    /**
     * 将指定的多个全路径文件 压缩成zip文件到指定文件路径 文件名后缀为zip
     *
     * @Title: zip
     * @Description: TODO
     * @param filePaths 需要压缩的文件地址列表（绝对路径）
     * @param zipFilePath 需要压缩到哪个zip文件（无需创建这样一个zip，只需要指定一个全路径）
     * @throws IOException
     */
    public static String zip(List<String> filePaths, String zipFilePath) throws IOException {
        byte[] buf = new byte[1024];
        File zipFile = new File(zipFilePath);
        //zip文件不存在，则创建文件，用于压缩
        if(!zipFile.exists()) {
            zipFile.createNewFile();
        }

        int fileCount = 0;//记录压缩了几个文件？
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for(int i = 0; i < filePaths.size(); i++){
                String relativePath = filePaths.get(i);
                if(StringUtils.isEmpty(relativePath)){
                    continue;
                }
                File sourceFile = new File(relativePath);//绝对路径找到file
                if(sourceFile == null || !sourceFile.exists()){
                    continue;
                }
                FileInputStream fis = new FileInputStream(sourceFile);
                zos.putNextEntry(new ZipEntry(sourceFile.getName()));
                int len;
                while((len = fis.read(buf)) > 0){
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                fis.close();
                fileCount++;
            }
            zos.close();
            //System.out.println("压缩完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zipFile.getName();
    }

    /**
     * 解压文件-- 未测
     *
     * @param filePath
     *      压缩文件路径
     */
    public static void unzip(String filePath) throws IOException {
        File source = new File(filePath);
        if (source.exists()) {
            ZipInputStream zis = null;
            BufferedOutputStream bos = null;
            try {
                zis = new ZipInputStream(new FileInputStream(source));
                ZipEntry entry = null;
                while ((entry = zis.getNextEntry()) != null
                        && !entry.isDirectory()) {
                    File target = new File(source.getParent(), entry.getName());
                    if (!target.getParentFile().exists()) {
                        // 创建文件父目录
                        target.getParentFile().mkdirs();
                    }
                    // 写入文件
                    bos = new BufferedOutputStream(new FileOutputStream(target));
                    int read = 0;
                    byte[] buffer = new byte[1024 * 10];
                    while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.flush();
                }
                zis.closeEntry();
            } finally {
                close(zis, bos);
            }
        }
    }

    /**
     * 关闭一个或多个流对象
     *
     * @param closeables
     *      可关闭的流对象列表
     * @throws IOException
     */
    public static void close(Closeable... closeables) throws IOException {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
//        String targetPath = "D:\\ruoyi\\uploadPath\\download";
//        File file = ZipUtil.zip(targetPath);
//        System.out.println(file);
//        ZipUtil.unzip("D:\\ruoyi\\uploadPath\\download.zip");

        List<String> fileList = new ArrayList<>();
        fileList.add("D:\\ruoyi\\uploadPath\\download\\792b3d43-c25e-4bf7-a42b-d9283ff9b94d_预警列表导出.xlsx");
        fileList.add("D:\\ruoyi\\uploadPath\\download\\b6e779b4-0845-4a17-9d1b-7b7a4eaa29af_nox超标排放异常.xlsx");
        fileList.add("D:\\ruoyi\\uploadPath\\download\\82cac1db-9188-4c8d-a03f-d5a4cb29475c_排放异常.xlsx");
        fileList.add("D:\\ruoyi\\uploadPath\\download\\5369c5da-b3c7-4d33-928a-ee7cc3b2a7a2_关联车辆.xlsx");
        String fileNem = ZipUtil.zip(fileList, "D:\\ruoyi\\uploadPath\\download\\1111.zip");
        System.out.println(fileNem);
    }

}
