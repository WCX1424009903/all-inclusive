package org.example.minio.utils;

import lombok.Getter;
import lombok.Setter;

/**
*文件信息
* @author wcx
* @date 2022/12/14
*/
@Setter
@Getter
public class Fileinfo {
    /**
    * 文件名
    */
    String filename;

    /**
    * 是否目录
    */
    Boolean directory;
}
