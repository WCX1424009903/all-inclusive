package com.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;

import java.io.*;
import java.util.Map;

/**
 * @author junjie ma
 * @date 2021-4-22 8:12
 */
@Slf4j
public class WordTemplate {

    private String path;

    HWPFDocument document;

    private Throwable ex;

    /**
     * 通过模板Word的路径初始化
     * */
    public WordTemplate(String path) {
        this.path = path;
        init();
    }

    private void init() {
        File file = new File(path);
        if (file.exists() && (path == null
                || (!path.endsWith(".doc") && !path.endsWith(".docx"))))
            ex = new IOException("错误的文件格式");
        else{
            try (InputStream is = new FileInputStream(file)){
                document = new HWPFDocument(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 验证word模板是否可用
     * @return true-可用 false-不可用
     * */
    public boolean examine(){
        if(ex == null && document != null)
            return true;
        return false;
    }


    /**
     * 替换word中使用固定模板${xxx}
     * @param contentMap 替换固定模板${}的值
     */
    public void replaceTemplateByText(Map<String, String> contentMap) {

        // 读取文本内容
        Range bodyRange = document.getRange();

        // 替换内容
        for (Map.Entry<String, String> entry : contentMap.entrySet()) {
            bodyRange.replaceText("${" + entry.getKey() + "}", entry.getValue()==null?"":entry.getValue());
        }

    }


    /**
     * 存储Excel
     *
     * @param path 存储路径
     * @throws IOException
     */
    public void save(String path) {
        if(!examine())
            return;
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try ( FileOutputStream fos = new FileOutputStream(path)) {
            document.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
