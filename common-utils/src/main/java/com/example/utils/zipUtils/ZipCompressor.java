package com.example.utils.zipUtils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩工具类
 * @author junjie ma
 * @date 2021-5-6 11:13
 */
public class ZipCompressor {
    static final int BUFFER = 8192;

//    private File zipFile;

//    public ZipCompressor2(String pathName) {
//        zipFile = new File(pathName);
//    }

    /**
     * 压缩很多文件到一个文件
     * @param descName 全路径压缩后的文件名
     * @param pathName 需要打包压缩的文件
     */
    public static void compressMany(String descName, String... pathName) {

        File zipFile = new File(descName);

        ZipOutputStream out = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());
            out = new ZipOutputStream(cos);
            String basedir = "";
            for (int i=0;i<pathName.length;i++){
                compress(new File(pathName[i]), out, basedir);
            }
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 压缩一个文件
     * @param descName 全路径压缩后的文件名
     * @param srcPathName 压缩的目标文件
     */
    public static void compressOne(String descName, String srcPathName) {

        File zipFile = new File(descName);

        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,
                    new CRC32());
            ZipOutputStream out = new ZipOutputStream(cos);
            String basedir = "";
            compress(file, out, basedir);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void compress(File file, ZipOutputStream out, String basedir) {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            compressDirectory(file, out, basedir);
        } else {
            compressFile(file, out, basedir);
        }
    }

    /** 压缩一个目录 */
    private static void compressDirectory(File dir, ZipOutputStream out, String basedir) {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            /* 递归 */
            compress(files[i], out, basedir + dir.getName() + "/");
        }
    }

    /** 压缩一个文件 */
    private static void compressFile(File file, ZipOutputStream out, String basedir) {
        if (!file.exists()) {
            return;
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basedir + file.getName());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            bis.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String zipName = "D:\\ruoyi\\uploadPath\\download\\" + DateUtil.today().replaceAll("-","") + "-" + IdUtil.simpleUUID().substring(0,8) + ".zip";
        compressMany(zipName, "D:\\ruoyi\\uploadPath\\download\\189415941","D:\\ruoyi\\uploadPath\\download\\189415943");
    }

}