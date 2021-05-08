package com.example.utils;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author junjie ma
 * @date 2021-4-21 10:42
 */

/**
 * 使用一个已经存在的Excel作为模板，可以对当前的模板Excel进行修改操作，
 * 然后重新输出为流，或者存入文件系统当中。
 *
 * @author: jyb
 * @Description: excel模板操作
 * @Email: 253684597@qq.com
 * */
public class ExcelTemplate {

    private String path;

    private Workbook workbook;

    private Sheet[] sheets;

    private Sheet sheet;

    private Throwable ex;

    private List<Cell> cellList = null;

    private Pattern doublePattern = Pattern.compile("^[0-9]+[.]{0,1}[0-9]*[dD]{0,1}$");

    /**
     * 通过模板Excel的路径初始化
     * */
    public ExcelTemplate(String path) {
        this.path = path;
        init();
    }

    public ExcelTemplate(InputStream is) {
        init(is);
    }

    private void init(){
        File file = new File(path);
        if (file.exists() && (path == null
                || (!path.endsWith(".xlsx") && !path.endsWith(".xls"))))
            ex = new IOException("错误的文件格式");
        else{
            try (InputStream is = new FileInputStream(file)){
                workbook = WorkbookFactory.create(is);
                sheets = new Sheet[workbook.getNumberOfSheets()];
                for(int i = 0;i < sheets.length;i++){
                    sheets[i] = workbook.getSheetAt(i);
                }
                if(sheets.length > 0)
                    sheet = sheets[0];
                sheet.setForceFormulaRecalculation(true);
            } catch (EncryptedDocumentException e) {
                ex = e;
            } catch (IOException e) {
                ex = e;
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void init(InputStream is){
        try {
            workbook = WorkbookFactory.create(is);
            sheets = new Sheet[workbook.getNumberOfSheets()];
            for(int i = 0;i < sheets.length;i++){
                sheets[i] = workbook.getSheetAt(i);
            }
            if(sheets.length > 0)
                sheet = sheets[0];
            sheet.setForceFormulaRecalculation(true);
        } catch (EncryptedDocumentException e) {
            ex = e;
        } catch (IOException e) {
            ex = e;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
    }

    private boolean initSheet(int sheetNo){
        if(!examine() || sheetNo < 0 || sheetNo > workbook.getNumberOfSheets() - 1)
            return false;
        int sheetNum = workbook.getNumberOfSheets();
        sheets = new Sheet[sheetNum];
        for(int i = 0;i < sheetNum;i++){
            if(i == sheetNo)
                sheet = workbook.getSheetAt(i);
            sheets[i] = workbook.getSheetAt(i);
        }
        sheet = workbook.getSheetAt(sheetNo);
        sheet.setForceFormulaRecalculation(true);
        return true;
    }

    /**
     * 验证模板是否可用
     * @return true-可用 false-不可用
     * */
    public boolean examine(){
        if(ex == null && workbook != null)
            return true;
        return false;
    }

    private boolean examineSheetRow(int index){
        if(index < 0 || index > sheet.getLastRowNum())
            return false;
        return true;
    }

    /**
     * 使用一个已经存在的row作为模板，
     * 从sheet[sheetNo]的toRowNum行开始插入这个row模板的副本
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param fromRowStartIndex 模板row区域的开始索引
     * @param fromRowEndIndex 模板row区域的结束索引
     * @param toRowIndex 开始插入的row索引值
     * @param copyNum 复制的数量
     * @param delRowTemp 是否删除模板row区域
     * @return int 插入的行数量
     * @throws IOException
     * */
    public int addRowByExist(int sheetNo,int fromRowStartIndex, int fromRowEndIndex,int toRowIndex, int copyNum,boolean delRowTemp)
            throws IOException {
        LinkedHashMap<Integer, LinkedList<String>> map = new LinkedHashMap<>();
        for(int i = 1;i <= copyNum;i++){
            map.put(i,new LinkedList<>());
        }
        return addRowByExist(sheetNo,fromRowStartIndex,fromRowEndIndex,toRowIndex,map,delRowTemp);
    }

    /**
     * 使用一个已经存在的row作为模板，
     * 从sheet[sheetNo]的toRowNum行开始插入这个row模板的副本,
     * 并且使用areaValue从左至右，从上至下的替换掉
     * row区域中值为 ${} 的单元格的值
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param fromRowIndex 模板行的索引
     * @param toRowIndex 开始插入的row索引
     * @param areaValues 替换模板row区域的${}值
     * @return int 插入的行数量
     * @throws IOException
     * */
    public int addRowByExist(int sheetNo,int fromRowIndex, int toRowIndex,
                             LinkedHashMap<Integer,LinkedList<String>> areaValues)
            throws IOException {
        return addRowByExist(sheetNo,fromRowIndex,fromRowIndex,toRowIndex,areaValues,true);
    }

    /**
     * 使用一个已经存在的行区域作为模板，
     * 从sheet的toRowNum行开始插入这段行区域,
     * areaValue会从左至右，从上至下的替换掉
     * row区域中值为 ${} 的单元格的值
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param fromRowStartIndex 模板row区域的开始索引
     * @param fromRowEndIndex 模板row区域的结束索引
     * @param toRowIndex 开始插入的row索引
     * @param areaValues 替换模板row区域的${}值
     * @param delRowTemp 是否删除模板row区域
     * @return int 插入的行数量
     * @throws IOException
     * */
    public int addRowByExist(int sheetNo,int fromRowStartIndex, int fromRowEndIndex,int toRowIndex,
                             LinkedHashMap<Integer,LinkedList<String>> areaValues, boolean delRowTemp)
            throws IOException {
        exception();
        if(!examine()
                || !initSheet(sheetNo)
                || !examineSheetRow(fromRowStartIndex)
                || !examineSheetRow(fromRowEndIndex)
                || fromRowStartIndex > fromRowEndIndex)
            return 0;
        int areaNum;List<Row> rows = new ArrayList<>();
        if(areaValues != null){
            int n = 0,f = areaValues.size() * (areaNum = (fromRowEndIndex - fromRowStartIndex + 1));
            // 在插入前腾出空间，避免新插入的行覆盖原有的行
            shiftAndCreateRows(sheetNo,toRowIndex,f);
            // 读取需要插入的数据
            for (Integer key:areaValues.keySet()){
                List<Row> temp = new LinkedList<>();
                // 插入行
                for(int i = 0;i < areaNum;i++){
                    int num = areaNum * n + i;
                    Row toRow = sheet.getRow(toRowIndex + num);
                    Row row;
                    if(toRowIndex >= fromRowEndIndex)
                        row = copyRow(sheetNo,sheet.getRow(fromRowStartIndex + i),sheetNo,toRow,true,true);
                    else
                        row = copyRow(sheetNo,sheet.getRow(fromRowStartIndex + i + f),sheetNo,toRow,true,true);
                    temp.add(row);
                }
                // 使用传入的值覆盖${}或者N${}
                replaceMark(temp,areaValues.get(key));
                rows.addAll(temp);
                n++;
            }
            if(delRowTemp){
                if(toRowIndex >= fromRowEndIndex)
                    removeRowArea(sheetNo,fromRowStartIndex,fromRowEndIndex);
                else
                    removeRowArea(sheetNo,fromRowStartIndex + f,fromRowEndIndex + f);
            }
        }
        return rows.size();
    }

    /**
     * 使用一个已经存在的列区域作为模板，
     * 从sheet的toColumnIndex列开始插入这段列区域,
     * areaValue会从上至下，从左至右的替换掉列区域
     * 中值为 ${} 的单元格的值
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param fromColumnStartIndex 模板列区域的开始索引
     * @param fromColumnEndIndex 模板列区域的结束索引
     * @param toColumnIndex 开始插入的列索引
     * @param areaValues 替换模板列区域的${}值
     * @param delColumnTemp 是否删除模板列区域
     * @return int 插入的列数量
     * @throws IOException
     * */
    public int addColumnByExist(int sheetNo,int fromColumnStartIndex, int fromColumnEndIndex,int toColumnIndex,
                                LinkedHashMap<Integer,LinkedList<String>> areaValues, boolean delColumnTemp)
            throws IOException{
        exception();
        if(!examine()
                || !initSheet(sheetNo)
                || fromColumnStartIndex > fromColumnEndIndex
                || toColumnIndex < 0)
            return 0;
        // 合并区域的列的数量
        int areaNum;
        List<Integer> n = new ArrayList<>();
        n.add(0);
        if(areaValues != null){
            int f = areaValues.size() * (areaNum = (fromColumnEndIndex - fromColumnStartIndex + 1));
            // 创建空白的列
            shiftAndCreateColumns(sheetNo,toColumnIndex-1,f);
            // 获取所有合并区域
            List<CellRangeAddress> crds = sheet.getMergedRegions();
            // 读取需要插入的数据
            for (Integer key:areaValues.keySet()){
                for(int i = 0;i < areaNum;i++){
                    // 获取插入的位置
                    int position = toColumnIndex + n.get(0) * areaNum + i;
                    // 插入的列的位置是在复制区域之后
                    if(toColumnIndex >= fromColumnStartIndex)
                        copyColumn(sheetNo,fromColumnStartIndex + i,sheetNo,position,true);
                        // 插入的列的位置是在复制区域之前
                    else
                        copyColumn(sheetNo,fromColumnStartIndex + i + f,sheetNo,position,true);
                }
                // 复制源列的合并区域到新添加的列
                if(crds != null){
                    crds.forEach(crd -> {
                        // 列偏移量
                        int offset = toColumnIndex - fromColumnStartIndex + areaNum * n.get(0);
                        // 合并区域的宽度
                        int rangeAreaNum = crd.getLastColumn() - crd.getFirstColumn() + 1;
                        // 原合并区域的首列
                        int firstColumn = crd.getFirstColumn();
                        // 需要添加的合并区域首列
                        int addFirstColumn = firstColumn + offset;
                        // 根据插入的列的位置是在复制区域之前还是之后
                        // firstColumn和addFirstColumn分配不同的值
                        firstColumn = toColumnIndex >= fromColumnStartIndex ? firstColumn : firstColumn - f;
                        addFirstColumn = toColumnIndex >= fromColumnStartIndex ? addFirstColumn : toColumnIndex + areaNum * n.get(0);
                        if(firstColumn >= fromColumnStartIndex && firstColumn < fromColumnEndIndex){
                            if ((firstColumn + rangeAreaNum - 1) > fromColumnEndIndex)
                                rangeAreaNum = fromColumnEndIndex - firstColumn + 1;
                            if(rangeAreaNum > areaNum){
                                mergedRegion(sheetNo,
                                        crd.getFirstRow(),
                                        crd.getLastRow(),
                                        addFirstColumn,
                                        addFirstColumn + areaNum - 1);
                            }
                            else {
                                mergedRegion(sheetNo,
                                        crd.getFirstRow(),
                                        crd.getLastRow(),
                                        addFirstColumn,
                                        addFirstColumn + rangeAreaNum - 1);
                            }
                        }
                    });
                }
                // 填充${}
                List<String> fillValues = new ArrayList<>(areaValues.get(key));
                if (fillValues == null || fillValues.size() == 0){
                    n.replaceAll(i -> i + 1);
                    continue;
                }
                List<Cell> needFillCells;
                initCellList(sheetNo);
                needFillCells = cellList;
                // 获取所有的值为${}单元格
                needFillCells = needFillCells.stream().filter(c -> {
                    if(c != null && c.getCellTypeEnum() == CellType.STRING){
                        if ("${}".equals(c.getStringCellValue()) || "N${}".equals(c.getStringCellValue()))
                            return true;
                    }
                    return false;
                }).collect(Collectors.toList());
                if (needFillCells == null){
                    n.replaceAll(i -> i + 1);
                    continue;
                }
                // 所有的${}单元格按照列从小到大，行从小到大的顺序排序
                needFillCells.sort((c1,c2) -> {
                    if (c1 == null && c2 == null) {
                        return 0;
                    }
                    if (c1 == null) {
                        return 1;
                    }
                    if (c2 == null) {
                        return -1;
                    }
                    if(c1.getColumnIndex() > c2.getColumnIndex())
                        return 1;
                    else if(c1.getColumnIndex() < c2.getColumnIndex())
                        return -1;
                    else {
                        if(c1.getRowIndex() > c2.getRowIndex())
                            return 1;
                        else if(c1.getRowIndex() < c2.getRowIndex())
                            return -1;
                        else
                            return 0;
                    }
                });
                needFillCells
                        .stream()
                        .filter(c -> {
                            if(c == null)
                                return false;
                            // 筛选出当前需要填充的单元格
                            return c.getColumnIndex() >= toColumnIndex + areaNum * n.get(0)
                                    && c.getColumnIndex() <= toColumnIndex + areaNum * (n.get(0) + 1);
                        }).forEach(c -> {
                    if(fillValues.size() > 0){
                        // 设置为列的首行，再移除掉首行的值
                        String value = fillValues.stream().filter(Objects::nonNull).findFirst().orElse("");
                        if (doublePattern.matcher(value == null ? "": value).find()){
                            c.setCellValue(Double.parseDouble(value));
                        }
                        else {
                            c.setCellValue(value);
                        }
                        CellStyle cellStyle = c.getCellStyle();
                        cellStyle.setWrapText(true);
                        c.setCellStyle(cellStyle);
                        fillValues.remove(0);
                    }
                });
                n.replaceAll(i -> i + 1);
            }
            if(delColumnTemp){
                if(toColumnIndex >= fromColumnStartIndex)
                    removeColumnArea(sheetNo,fromColumnStartIndex,fromColumnEndIndex);
                else
                    removeColumnArea(sheetNo,fromColumnStartIndex + f,fromColumnEndIndex + f);
            }
        }
        return n.get(0);
    }

    /**
     * 使用一个已经存在的列区域作为模板，
     * 从sheet的toColumnIndex列开始插入这段列区域
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param fromColumnStartIndex 模板列区域的开始索引
     * @param fromColumnEndIndex 模板列区域的结束索引
     * @param toColumnIndex 开始插入的列索引
     * @param copyNum 复制数量
     * @param delColumnTemp 是否删除模板列区域
     * @return int 插入的列数量
     * @throws IOException
     * */
    public int addColumnByExist(int sheetNo,int fromColumnStartIndex, int fromColumnEndIndex,int toColumnIndex,
                                int copyNum, boolean delColumnTemp)
            throws IOException{
        LinkedHashMap<Integer, LinkedList<String>> map = new LinkedHashMap<>();
        for(int i = 1;i <= copyNum;i++){
            map.put(i,new LinkedList<>());
        }
        return addColumnByExist(sheetNo,fromColumnStartIndex,fromColumnEndIndex,toColumnIndex,map,delColumnTemp);
    }

    /**
     * 填充Excel当中的变量
     *
     * @param fillValues 填充的值
     * @return int 受影响的变量数量
     * @throws IOException
     **/
    public int fillVariable(Map<String,String> fillValues) throws IOException {
        return fillVariable(0,fillValues);
    }

    /**
     * 填充Excel当中的变量
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param fillValues 填充的值
     * @return int 受影响的变量数量
     * @throws IOException
     **/
    public int fillVariable(int sheetNo,Map<String,String> fillValues)
            throws IOException {
        exception();
        if(!examine()
                || sheetNo < 0
                || sheetNo > sheets.length - 1
                || fillValues == null
                || fillValues.size() == 0)
            return 0;
        // 验证${}格式
        final Pattern pattern = Pattern.compile("(\\$\\{[^\\}]+})");
        // 把所有的${}按Cell分类，也就是说如果一个Cell中存在两个${}，
        // 这两个变量的Cell应该一样
        Map<Cell,Map<String,String>> cellVal = new HashMap<>();
        List<Integer> ns = new ArrayList<>();
        ns.add(0);
        fillValues.forEach((k,v) ->{
            // 找到变量所在的单元格
            Cell cell = findCells(sheetNo,s -> {
                if(s == null || "".equals(s))
                    return false;
                Matcher matcher = pattern.matcher(s);
                while(matcher.find()){
                    String variable = matcher.group(1);
                    if(variable != null
                            && formatParamCode(variable).equals(k.trim()))
                        return true;
                }
                return false;
            }).stream().findFirst().orElse(null);
            if(cell != null){
                Map<String,String> cellValMap = cellVal.get(cell);
                if(cellValMap == null)
                    cellValMap = new HashMap<>();
                cellValMap.put(k,v);
                cellVal.put(cell,cellValMap);
                ns.replaceAll(n -> n + 1);
            }
        });
        cellVal.forEach((k,v) -> {
            String cellValue = k.getStringCellValue();
            String value = composeMessage(cellValue,v);
            Matcher matcher = doublePattern.matcher(value == null ? "": value);
            if (matcher.find()){
                k.setCellValue(Double.parseDouble(value));
            }
            else
                k.setCellValue(value);
            CellStyle cellStyle = k.getCellStyle();
            cellStyle.setWrapText(true);
            k.setCellStyle(cellStyle);
        });
        return ns.get(0);
    }

    /**
     * 指定cell插入图片
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param imageBytes 图片字节流
     * @param imgType 图片类型（举例：png 传入 Workbook.PICTURE_TYPE_PNG作为参数）
     * @param startRow 开始行
     * @param endRow 结束行
     * @param startCol 开始列
     * @param endCol 结束列
     * */
    public void insertPicture(int sheetNo,byte[] imageBytes,int imgType,int startRow,int endRow,int startCol,int endCol) throws IOException {
        exception();
        if (!initSheet(sheetNo))
            return;

        Drawing patriarch = sheet.createDrawingPatriarch();
        ClientAnchor anchor = null;

        if (sheet instanceof XSSFSheet)
            anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) startCol, startRow, (short) endCol, endRow);
        else
            anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) startCol, startRow, (short) endCol, endRow);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

        patriarch.createPicture(anchor, workbook.addPicture(imageBytes, imgType));
        if (sheet instanceof  XSSFSheet) {
            List<XSSFShape> shapes = ((XSSFDrawing)patriarch).getShapes();
            for (XSSFShape shape : shapes) {
                XSSFPicture picture = (XSSFPicture) shape;
                picture.getPreferredSize();
            }
        }
        else {
            List<HSSFShape> list = ((HSSFSheet)sheet).getDrawingPatriarch().getChildren();
            for (HSSFShape shape : list) {
                if (shape instanceof HSSFPicture) {
                    HSSFPicture picture = (HSSFPicture) shape;
                    picture.getClientAnchor();
                    picture.getPictureData();
                }
            }
        }
    }

    /**
     * 根据行坐标和列坐标定位到单元格，填充单元格
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param rowIndex 行坐标
     * @param columnIndex 列坐标
     * @param value 填充的值
     * @return boolean 是否成功
     * @throws IOException
     **/
    public boolean fillByCoordinate(int sheetNo,int rowIndex,int columnIndex,String value)
            throws IOException {
        exception();
        if(!initSheet(sheetNo))
            return false;
        Row row = sheet.getRow(rowIndex);
        if(row == null)
            return false;
        Cell cell = row.getCell(columnIndex);
        if(cell == null)
            return false;
        if (doublePattern.matcher(value == null ? "": value).find()){
            cell.setCellValue(Double.parseDouble(value));
        }
        else{
            cell.setCellValue(value);
        }
        return true;
    }

    /**
     * 根据断言predicate查找sheet当中符合条件的cell
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param predicate 筛选的断言
     * @return List<Cell> 符合条件的Cell
     * */
    public List<Cell> findCells(int sheetNo,Predicate<String> predicate){
        Objects.requireNonNull(predicate);
        initCellList(sheetNo);
        return cellList.stream()
                .map(c -> {
                    if(c != null && c.getCellTypeEnum() == CellType.STRING)
                        return c.getStringCellValue();
                    return null;
                })// Cell流转换为String流
                .filter(predicate)
                .map(s -> cellList.stream().filter(c -> {
                    if(c != null && c.getCellTypeEnum() == CellType.STRING
                            && s.equals(c.getStringCellValue()))
                        return true;
                    return false;
                }).findFirst().orElse(null))// String流重新转换位Cell流
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    /**
     * 根据断言predicate查找sheet当中符合条件的Row
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param predicate 筛选的断言
     * @return List<Row> 符合条件的Row
     * */
    public List<Row> findRows(int sheetNo,Predicate<Row> predicate){
        if(!examine() || !initSheet(sheetNo))
            return null;
        List<Row> rows = new ArrayList<>();
        for(int i = sheet.getFirstRowNum();i <= sheet.getLastRowNum();i++){
            Row row = sheet.getRow(i);
            if(predicate.test(row))
                rows.add(row);
        }
        return rows;
    }

    /**
     * 提取变量中的值，比如 formatParamCode("${1234}"),
     * 会得到结果1234
     *
     * @param paramCode 需要提取的字符串
     * @return String
     * */
    private String formatParamCode(String paramCode){
        if(paramCode == null)
            return "";
        return paramCode.replaceAll("\\$", "")
                .replaceAll("\\{", "")
                .replaceAll("\\}", "");
    }

    /**
     * 使用paramData当中的值替换data当中的变量
     *
     * @param data 需要提取的字符串
     * @param paramData 需要替换的值
     * @return String
     * */
    private String composeMessage(String data, Map<String,String> paramData){
        String regex = "\\$\\{(.+?)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data == null ? "": data);
        StringBuffer msg = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);// 键名
            String value = paramData.get(key);// 键值
            if(value == null) {
                value = "";
            } else {
                value = value.replaceAll("\\$", "\\\\\\$");
            }
            matcher.appendReplacement(msg, value);
        }
        matcher.appendTail(msg);
        return msg.toString();
    }

    /**
     * 计算excel公式中的单元格的列和行加上数值后的结果
     *
     * @param isColumn 是否是行的计算
     * @param value 原始值
     * @param addNum 添加的数量
     * @return String
     * */
    private String addRowOrColumnIndex(boolean isColumn,String value,int addNum){
        value = value == null ? "" : value;
        if(isColumn){
            if (!Pattern.compile("^[A-Z]+$").matcher(value).find())
                return value;
            char[] cs = value.toCharArray();
            int cardinal = 0;
            // 组合转换为数字
            for (int i = cs.length - 1; i >= 0; i--) {
                cardinal += Math.pow(26,cs.length - 1 - i) * (cs[i] - 64);
            }
            // 加上添加后的数值
            cardinal += addNum;
            // 不能为0
            cardinal = cardinal <= 0 ? 1 : cardinal;
            // 是否需要向前借一位
            boolean borrowBit = false;
            Stack<Character> stack = new Stack<>();
            // 数字转换为组合
            while (true && cardinal > 0){
                int mode = cardinal % 26;
                // 如果到达了第一位
                if(cardinal >= 1 && cardinal < 26){
                    // 是否需要借位
                    if (borrowBit)
                        mode -= 1;
                    // 首位借位之后必须大于0才能添加
                    if (mode > 0)
                        stack.add((char)(mode + 64));
                    break;
                }
                cardinal -= mode;
                cardinal /= 26;
                if (borrowBit){
                    if (mode != 0)
                        mode -= 1;
                        // 如果借位的时候，发现本身也为0，需要向前再借位
                    else{
                        mode = 25;
                        borrowBit = true;
                        stack.add((char)(mode + 64));
                        continue;
                    }
                }
                if (mode == 0){
                    mode = 26;
                    borrowBit = true;
                }
                else
                    borrowBit = false;
                stack.add((char)(mode + 64));
            }
            int size = stack.size();
            char[] chars = new char[size];
            for (int j = size - 1; j >= 0; j--) {
                chars[size - 1 - j] = stack.get(j);
            }
            return new String(chars);
        }
        else {
            if(!Pattern.compile("^[0-9]+$").matcher(value).find())
                return value;
            try{
                int intValue = Integer.parseInt(value);
                intValue += addNum;
                if (intValue <= 0)
                    return "1";
                return Integer.toString(intValue);
            }catch (NumberFormatException e){
                return value;
            }
        }
    }

    /**
     * 修改公式里面单元格参数的坐标
     *
     * @param formula 公式
     * @param index 第几个单元格参数
     * @param rowAddNum 给行添加的数量
     * @param columnAddNum 给列添加的数量
     * @return String
     * */
    private String composeFormula(String formula,int index,
                                  int rowAddNum,int columnAddNum){
        String regex = "[A-Z]+[0-9]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(formula == null ? "" : formula);
        List<String> valueList = new LinkedList<>();
        String oldFormula = formula;
        while(matcher.find()){
            String value = matcher.group();
            valueList.add(value);
            formula = formula.replaceFirst(value,"@&");
        }
        if (index >= 0 && index < valueList.size()){
            String value = valueList.get(index);
            Matcher columnMatcher = Pattern.compile("[A-Z]+").matcher(value);
            String newValue = value;
            if (columnMatcher.find()){
                String columnIndex = columnMatcher.group();
                String rowIndex = value.replaceAll(columnIndex,"");
                columnIndex = addRowOrColumnIndex(true,columnIndex,columnAddNum);
                rowIndex = addRowOrColumnIndex(false,rowIndex,rowAddNum);
                newValue = columnIndex + rowIndex;
            }
            valueList.set(index,newValue);
        }
        String[] spilts = formula.split("@&");
        if (spilts.length == 0){
            if (valueList.size() == 1)
                return valueList.get(0);
            return oldFormula;
        }
        StringBuffer newFormula = new StringBuffer();
        int position = 0;
        for (int i = 0; i < spilts.length; i++) {
            newFormula.append(spilts[i]);
            if (position < valueList.size()){
                newFormula.append(valueList.get(position++));
            }
        }
        return newFormula.toString();
    }

    /**
     * 获取单元格里面公式的变量数量
     * 例如公式 SUM(AP40:AV40)，含有两个单元格变量 AP40和AV40，
     * 使用此方法会返回2
     *
     * @param cell 需要操作的单元格
     * @return int 单元格变量的数量
     * */
    public int getFormulaVariableNum(Cell cell){
        if (cell == null || cell.getCellTypeEnum() != CellType.FORMULA)
            return 0;
        String formula = cell.getCellFormula();
        Matcher matcher = Pattern.compile("[A-Z]+[0-9]+").matcher(formula == null ? "" : formula);
        int count = 0;
        while(matcher.find()){
            count++;
        }
        return count;
    }

    /**
     * 修改单元格的公式的参数
     * excel的所有列按照如下规则分布，
     * A,B,C,D...Z,AA,AB...AZ,BA,BB...BZ...以此类推，
     * 你可以看成是一个关于A,B,C...Z的排列组合问题
     *
     * 举例：
     * 单元格cell的公式为 SUM(AP40:AV40) 是求单元格 AP40 到 AV40的单元格的和，
     * 其中AP40中的AP表示单元格的列坐标，40表示横坐标，AV40类推。
     * 如果使用方法 composeCellFormula(cell,0,2,5)，则cell的公式会修改为 SUM(AU42:AV40)
     *
     * @param cell 需要修改的单元格
     * @param index 第几个单元格参数
     * @param rowAddNum 给行添加的数量
     * @param columnAddNum 给列添加的数量
     * @return String
     * */
    public void composeCellFormula(Cell cell,int index,
                                   int rowAddNum,int columnAddNum){
        if (cell == null || cell.getCellTypeEnum() != CellType.FORMULA)
            return;
        if (cell instanceof HSSFCell)
            throw new IllegalArgumentException("4.1.1及之前的版本的POI的处理xls文件的公式单元格有bug，建议换成xlsx文件，" +
                    "或者去官网查看哪个版本修复了这个bug(https://bz.apache.org/bugzilla/show_bug.cgi?id=64517)，换成此版本POI。" +
                    "如果引用的POI版本已经是修复了此BUG的版本的POI，可以删掉这个异常提示！");
        String formula = cell.getCellFormula();
        cell.setCellFormula(composeFormula(formula,index,rowAddNum,columnAddNum));
    }

    // 初始化cellList
    private void initCellList(int sheetNo){
        cellList = new ArrayList<>();
        if(examine() && !initSheet(sheetNo))
            return;
        int rn = sheet.getLastRowNum();
        for(int i = 0;i <= rn;i++){
            Row row = sheet.getRow(i);
            if(row != null){
                short cn = row.getLastCellNum();
                for (int j = 0;j < cn;j++){
                    cellList.add(row.getCell(j));
                }
            }
        }
    }

    /**
     * 替换掉所有行区域中的所有 ${} 标记
     * valueList对rows中${}替换的顺序是：
     * 从左至右，从上到下
     *
     * @param rows 行区域
     * @param vl 替换的值
     * */
    private void replaceMark(List<Row> rows,List<String> vl){
        if (rows == null || vl == null)
            return;
        List<String> valueList = new ArrayList<>(vl);
        rows.forEach(r -> {
            if(r != null){
                r.forEach(c -> {
                    if (c != null){
                        if (c.getCellTypeEnum() == CellType.STRING){
                            if("${}".equals(Optional.ofNullable(c.getStringCellValue()).orElse("").trim())){
                                if(valueList == null)
                                    return;
                                String value = valueList.stream().filter(Objects::nonNull).findFirst().orElse(null);
                                c.setCellValue(value);
                                CellStyle cellStyle = c.getCellStyle();
                                cellStyle.setWrapText(true);
                                c.setCellStyle(cellStyle);
                                if(value != null)
                                    valueList.remove(valueList.indexOf(value));
                            }
                            else if("N${}".equals(Optional.ofNullable(c.getStringCellValue()).orElse("").trim())){
                                if(valueList == null)
                                    return;
                                String value = valueList.stream().filter(Objects::nonNull).findFirst().orElse(null);
                                Matcher matcher = doublePattern.matcher(value == null ? "" : value);
                                if (matcher.find()){
                                    c.setCellValue(Double.parseDouble(value));
                                    CellStyle cellStyle = c.getCellStyle();
                                    cellStyle.setWrapText(true);
                                    c.setCellStyle(cellStyle);
                                    if(value != null)
                                        valueList.remove(valueList.indexOf(value));
                                }
                                else
                                    throw new IllegalArgumentException("N${} 所替换的内容只能为数字,非法参数\"" + value + "\"");
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * 复制Row到sheet中的另一个Row
     *
     * @param fromSheetNo 复制的行所在的sheet
     * @param fromRow 需要复制的行
     * @param toSheetNo 粘贴的行所在的sheet
     * @param toRow 粘贴的行
     * @param copyValueFlag 是否需要复制值
     * @param needMerged 是否需要合并单元格
     */
    private Row copyRow(int fromSheetNo,Row fromRow, int toSheetNo,Row toRow, boolean copyValueFlag,boolean needMerged) {
        if(fromSheetNo < 0 || fromSheetNo > workbook.getNumberOfSheets()
                || toSheetNo < 0 || toSheetNo > workbook.getNumberOfSheets())
            return null;
        if (fromRow == null)
            return null;
        if(toRow == null){
            Sheet sheet = workbook.getSheetAt(toSheetNo);
            if(sheet == null)
                return null;
            toRow = sheet.createRow(fromRow.getRowNum());
            if(toRow == null)
                return null;
        }
        // 设置高度
        toRow.setHeight(fromRow.getHeight());
        // 遍历行中的单元格
        for(Cell c:fromRow){
            Cell newCell = toRow.createCell(c.getColumnIndex());
            copyCell(c, newCell, copyValueFlag);
        }
        // 如果需要合并
        if(needMerged){
            Sheet fromSheet = workbook.getSheetAt(fromSheetNo);
            Sheet toSheet = workbook.getSheetAt(toSheetNo);
            // 遍历行当中的所有的合并区域
            List<CellRangeAddress> crds = fromSheet.getMergedRegions();
            if(crds != null && crds.size() > 0){
                for(CellRangeAddress crd : crds){
                    // 如果当前合并区域的首行为复制的源行
                    if(crd.getFirstRow() == fromRow.getRowNum()) {
                        // 创建对应的合并区域
                        CellRangeAddress newCellRangeAddress = new CellRangeAddress(
                                toRow.getRowNum(),
                                (toRow.getRowNum() + (crd.getLastRow() - crd.getFirstRow())),
                                crd.getFirstColumn(),
                                crd.getLastColumn());
                        // 添加合并区域
                        safeMergedRegion(toSheetNo,newCellRangeAddress);
                    }
                }
            }
        }
        return toRow;
    }

    /**
     * 复制sheet中列的另一列
     *
     * @param fromSheetNo 复制的行所在的sheet
     * @param fromColumnIndex 需要复制的行索引
     * @param toSheetNo 粘贴的行所在的sheet
     * @param toColumnIndex 粘贴的行
     * @param copyValueFlag 是否需要复制值
     */
    private void copyColumn(int fromSheetNo,int fromColumnIndex,int toSheetNo,
                            int toColumnIndex,boolean copyValueFlag) {
        if(fromSheetNo < 0 || fromSheetNo > workbook.getNumberOfSheets()
                || toSheetNo < 0 || toSheetNo > workbook.getNumberOfSheets())
            return;
        Sheet fromSheet = workbook.getSheetAt(fromSheetNo);
        Sheet toSheet = workbook.getSheetAt(toSheetNo);
        for(int i = 0;i <= fromSheet.getLastRowNum();i++){
            Row fromRow = fromSheet.getRow(i);
            Row toRow = toSheet.getRow(i);
            if(fromRow == null)
                continue;
            if(toRow == null)
                toRow = toSheet.createRow(i);
            if(toRow == null)
                continue;
            // 设置高度
            toRow.setHeight(fromRow.getHeight());
            Cell srcCell = fromRow.getCell(fromColumnIndex);
            Cell distCell = toRow.getCell(toColumnIndex);
            if(srcCell == null)
                continue;
            if(distCell == null)
                distCell = toRow.createCell(toColumnIndex);
            // 设置列宽
            toSheet.setColumnWidth(toColumnIndex,fromSheet.getColumnWidth(fromColumnIndex));
            copyCell(srcCell,distCell,copyValueFlag);
        }
    }

    /**
     * 复制Cell到sheet中的另一个Cell
     *
     * @param srcCell 需要复制的单元格
     * @param distCell 粘贴的单元格
     * @param copyValueFlag true则连同cell的内容一起复制
     */
    private void copyCell(Cell srcCell, Cell distCell, boolean copyValueFlag) {
        if (srcCell == null || distCell == null)
            return;

        // 获取源单元格的样式
        CellStyle srcStyle = srcCell.getCellStyle();
        // 复制样式
        distCell.setCellStyle(srcStyle);

        // 复制评论
        if(srcCell.getCellComment() != null) {
            distCell.setCellComment(srcCell.getCellComment());
        }
        // 不同数据类型处理
        CellType srcCellType = srcCell.getCellTypeEnum();
        if(copyValueFlag) {
            if(srcCellType == CellType.NUMERIC) {
                if(DateUtil.isCellDateFormatted(srcCell)) {
                    distCell.setCellValue(srcCell.getDateCellValue());
                } else {
                    distCell.setCellValue(srcCell.getNumericCellValue());
                }
            } else if(srcCellType == CellType.STRING) {
                distCell.setCellValue(srcCell.getRichStringCellValue());
            } else if(srcCellType == CellType.BLANK) {

            } else if(srcCellType == CellType.BOOLEAN) {
                distCell.setCellValue(srcCell.getBooleanCellValue());
            } else if(srcCellType == CellType.ERROR) {
                distCell.setCellErrorValue(srcCell.getErrorCellValue());
            } else if(srcCellType == CellType.FORMULA) {
                distCell.setCellFormula(srcCell.getCellFormula());
            } else {
            }
        }
    }

    /**
     * 合并单元格区域，本方法是安全的操作，在出现合并冲突的时候，
     * 分割合并区域，然后最大限度的合并冲突区域
     *
     * 使用此方法而不是采用addMergedRegion()和
     * addMergedRegionUnsafe()合并单元格区间，
     * 因为此方法会自行解决合并区间冲突，避免报错或者生成
     * 无法打开的excel
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param firstRow 开始行
     * @param lastRow 结束行
     * @param firstCol 开始列
     * @param lastCol 结束列
     * */
    public void mergedRegion(int sheetNo,int firstRow, int lastRow, int firstCol, int lastCol){
        if(firstRow > lastRow || firstCol > lastCol)
            return;
        CellRangeAddress address = new CellRangeAddress(firstRow,lastRow,firstCol,lastCol);
        safeMergedRegion(sheetNo,address);
    }

    /**
     * 合并单元格区域，本方法是安全的操作，在出现合并冲突的时候，
     * 分割合并区域，然后最大限度的合并冲突区域
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param rangeAddress 合并的单元格区域
     * */
    private void safeMergedRegion(int sheetNo,CellRangeAddress rangeAddress){
        if(!examine() || !initSheet(sheetNo) || rangeAddress == null)
            return;
        // 获取所有合并的区域
        List<CellRangeAddress> crds = sheet.getMergedRegions();
        if(crds == null)
            return;
        // 获取描述单元格区域的坐标，
        // 在首行和首列，坐标等于行编号，
        // 在末行和末列，坐标等于行编号加1
        int firstRow = rangeAddress.getFirstRow();
        int lastRow = rangeAddress.getLastRow() + 1;
        int firstColumn = rangeAddress.getFirstColumn();
        int lastColumn = rangeAddress.getLastColumn() + 1;
        // 查找冲突的单元格区域
        CellRangeAddress conflictRange = crds.stream()
                .filter(crd -> {
                    // 获取单元格区域的坐标
                    int cFirstRow = crd.getFirstRow();
                    int cLastRow = crd.getLastRow() + 1;
                    int cFirstColumn = crd.getFirstColumn();
                    int cLastColumn = crd.getLastColumn()  + 1;
                    // 每个合并单元格区域看成一个长方形
                    // 计算两个长方形中心的X坐标的距离
                    float xDistance = (float)(lastColumn + firstColumn)/2
                            - (float)(cLastColumn + cFirstColumn)/2;
                    // 每个合并单元格区域看成一个长方形
                    // 计算两个长方形中心的Y坐标的距离
                    float yDistance = (float)(lastRow + firstRow)/2
                            - (float)(cLastRow + cFirstRow)/2;
                    // 获取距离的绝对值
                    xDistance = xDistance >= 0 ? xDistance : -xDistance;
                    yDistance = yDistance >= 0 ? yDistance : -yDistance;
                    // 如果两个合并区域相交了，返回true
                    if(xDistance < ((float)(lastColumn - firstColumn)/2 + (float)(cLastColumn - cFirstColumn)/2)
                            && yDistance < ((float)(lastRow - firstRow)/2 + (float)(cLastRow - cFirstRow)/2))
                        return true;
                    return false;
                })
                .findFirst()
                .orElse(null);
        // 如果没有查找到冲突的区域，直接合并
        if(conflictRange == null){
            if(examineRange(rangeAddress))
                sheet.addMergedRegion(rangeAddress);
        }
        // 如果合并区域冲突了，分离新增的合并区域
        List<CellRangeAddress> splitRangeAddr = splitRangeAddress(conflictRange,rangeAddress);
        if(splitRangeAddr != null)
            splitRangeAddr.forEach(sra -> safeMergedRegion(sheetNo,sra));
    }

    /**
     * 如果插入的目标合并区域target和sheet中已存在的合并区域source冲突，
     * 把target分割成多个合并区域，这些合并区域都不会和source冲突
     *
     * @param source 已经存在的合并单元格区域
     * @param target 新增的合并单元格区域
     * @return target分离之后的合并单元格列表
     * */
    private List<CellRangeAddress> splitRangeAddress(CellRangeAddress source,CellRangeAddress target){
        List<CellRangeAddress> splitRangeAddr = null;
        if(source == null || target == null)
            return null;
        // 获取source区域的坐标
        int sFirstRow = source.getFirstRow();
        int sLastRow = source.getLastRow() + 1;
        int sFirstColumn = source.getFirstColumn();
        int sLastColumn = source.getLastColumn() + 1;
        // 获取target区域的坐标
        int tFirstRow = target.getFirstRow();
        int tLastRow = target.getLastRow() + 1;
        int tFirstColumn = target.getFirstColumn();
        int tLastColumn = target.getLastColumn() + 1;

        while(true){
            if(splitRangeAddr == null)
                splitRangeAddr = new ArrayList<>();
            // 如果target被切分得无法越过source合并区域，退出循环
            if(tFirstRow >= sFirstRow && tLastRow <= sLastRow
                    && tFirstColumn >= sFirstColumn && tLastColumn <= sLastColumn)
                break;
            // 只考虑Y坐标，当source的最大Y坐标sLastRow在开区间(tFirstRow,tLastRow)
            if(sLastRow > tFirstRow && sLastRow < tLastRow){
                CellRangeAddress address =
                        new CellRangeAddress(sLastRow,tLastRow - 1,tFirstColumn,tLastColumn - 1);
                tLastRow = sLastRow;
                if(examineRange(address))
                    splitRangeAddr.add(address);
            }
            // 只考虑Y坐标，当source的最小Y坐标sFirstRow在开区间(tFirstRow,tLastRow)
            if(sFirstRow > tFirstRow && sFirstRow < tLastRow){
                CellRangeAddress address =
                        new CellRangeAddress(tFirstRow,sFirstRow - 1,tFirstColumn,tLastColumn - 1);
                tFirstRow = sFirstRow;
                if(examineRange(address))
                    splitRangeAddr.add(address);
            }
            // 只考虑X坐标，当source的最小X坐标sFirstColumn在开区间(tFirstColumn,tLastColumn)
            if(sFirstColumn > tFirstColumn && sFirstColumn < tLastColumn){
                CellRangeAddress address =
                        new CellRangeAddress(tFirstRow,tLastRow - 1,tFirstColumn,sFirstColumn - 1);
                tFirstColumn = sFirstColumn;
                if(examineRange(address))
                    splitRangeAddr.add(address);
            }
            // 只考虑X坐标，当source的最大X坐标sLastColumn在开区间(tFirstColumn,tLastColumn)
            if(sLastColumn > tFirstColumn && sLastColumn < tLastColumn){
                CellRangeAddress address =
                        new CellRangeAddress(tFirstRow,tLastRow - 1,sLastColumn,tLastColumn - 1);
                tLastColumn = sLastColumn;
                if(examineRange(address))
                    splitRangeAddr.add(address);
            }
        }
        return splitRangeAddr;
    }

    // 检查合并区域
    private boolean examineRange(CellRangeAddress address){
        if(address == null || !examine())
            return false;
        int firstRowNum = address.getFirstRow();
        int lastRowNum = address.getLastRow();
        int firstColumnNum = address.getFirstColumn();
        int lastColumnNum = address.getLastColumn();
        if(firstRowNum == lastRowNum && firstColumnNum == lastColumnNum)
            return false;
        return true;
    }

    private void exception() throws EncryptedDocumentException, IOException {
        if(ex != null){
            if(ex instanceof EncryptedDocumentException)
                throw new EncryptedDocumentException("无法读取的加密文件");
            else if(ex instanceof IOException)
                throw new IOException(ex);
            else
                return;
        }
    }

    /**
     * 把sheet[sheetNo]当中所有的行从startRow位置开始，
     * 全部下移moveNum数量的位置，并且在腾出的空间当中创建新行
     *
     * 应该使用本方法而不是采用sheet.shiftRows()和sheet.createRow()，
     * 主要是因为插入一段行的时候会进行如下步骤：
     * 第一：使用shiftRows腾出空间
     * 第二：使用createRow(position)从position开始创建行
     * 但是这样，后面下移的行的合并单元格会部分消失，
     * 并且新创建的行的合并单元格并没有消失，这是因为sheet当中的
     * 大于position的CellRangeAddress并没有跟着下移。
     * 而使用本方法下移并且在中间自动插入行，新插入的行不会含有任何合并单元格，
     * 并且原来的合并单元格也不会消失。
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param startRow 移动的Row区间的起始位置
     * @param moveNum 移动的行数
     * */
    public synchronized void shiftAndCreateRows(int sheetNo,int startRow,int moveNum){
        if(!examine() || !initSheet(sheetNo))
            return;

        // 复制当前需要操作的sheet到一个临时的sheet
        Sheet tempSheet = workbook.cloneSheet(sheetNo);
        // 获取临时sheet在workbook当中的索引
        int tempSheetNo = workbook.getSheetIndex(tempSheet);
        // 得到临时sheet的第一个row的索引
        int firstRowNum = tempSheet.getFirstRowNum();
        // 得到临时sheet的最后一个row的索引
        int lastRowNum = tempSheet.getLastRowNum();
        if(!clearSheet(sheetNo)){
            return;
        }
        if (startRow <= lastRowNum){
            for(int i= firstRowNum;i <= lastRowNum - firstRowNum + moveNum + 1;i++)
                sheet.createRow(i);
        }
        else {
            for(int i= firstRowNum;i <= startRow + moveNum + 1;i++)
                sheet.createRow(i);
        }
        for(int i= firstRowNum;i <= lastRowNum;i++){
            if(i < startRow)
                copyRow(tempSheetNo,tempSheet.getRow(i),sheetNo,sheet.getRow(i),true,true);
                // 到达需要插入的索引的位置，需要留出moveNum空间的行
            else
                copyRow(tempSheetNo,tempSheet.getRow(i),sheetNo,sheet.getRow(i + moveNum),true,true);
        }
        settingColumnWidth(tempSheetNo,sheetNo);
        // 删除临时的sheet
        workbook.removeSheetAt(tempSheetNo);
    }

    /**
     * 把sheet[sheetNo]当中所有的列从startColumn位置开始，
     * 全部右移moveNum数量的位置，并且在腾出的空间当中创建新列
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param startColumn 移动的列区间的起始位置
     * @param moveNum 移动的列数
     * */
    public synchronized void shiftAndCreateColumns(int sheetNo,int startColumn,int moveNum){
        if(!examine() || !initSheet(sheetNo))
            return;

        // 复制当前需要操作的sheet到一个临时的sheet
        Sheet tempSheet = workbook.cloneSheet(sheetNo);
        // 获取临时sheet在workbook当中的索引
        int tempSheetNo = workbook.getSheetIndex(tempSheet);
        // 得到临时sheet的第一个row的索引
        int firstRowNum = tempSheet.getFirstRowNum();
        // 得到临时sheet的最后一个row的索引
        int lastRowNum = tempSheet.getLastRowNum();

        if(!clearSheet(sheetNo)){
            return;
        }

        for(int i = firstRowNum;i <= lastRowNum;i++){
            Row row = tempSheet.getRow(i);
            if(row != null){
                int addNum = row.getLastCellNum() + moveNum;
                for(int j = 0;j < moveNum;j++){
                    row.createCell(addNum);
                }
                for(int j = 0;j <= row.getLastCellNum();j++){
                    if(j <= startColumn)
                        copyColumn(tempSheetNo,j,sheetNo,j,true);
                    else
                        copyColumn(tempSheetNo,j,sheetNo,j + moveNum,true);
                }
            }
        }
        List<CellRangeAddress> crds = tempSheet.getMergedRegions();
        if(crds == null)
            return;
        crds.forEach(crd -> {
            int firstColumn;
            int lastColumn;
            if((lastColumn = crd.getLastColumn()) <= startColumn)
                safeMergedRegion(sheetNo,crd);
            else if((firstColumn = crd.getFirstColumn()) <= startColumn){
                if(lastColumn > startColumn){
                    CellRangeAddress range = new CellRangeAddress(crd.getFirstRow(),crd.getLastRow(),firstColumn,startColumn);
                    if(examineRange(range))
                        safeMergedRegion(sheetNo,range);
                    range = new CellRangeAddress(crd.getFirstRow(),crd.getLastRow(),
                            startColumn + moveNum + 1,lastColumn + moveNum);
                    if(examineRange(range))
                        safeMergedRegion(sheetNo,range);
                }
            }
            else if(firstColumn > startColumn){
                CellRangeAddress range = new CellRangeAddress(crd.getFirstRow(),crd.getLastRow(),
                        firstColumn + moveNum,lastColumn + moveNum);
                if(examineRange(range))
                    safeMergedRegion(sheetNo,range);
            }
        });
        // 删除临时的sheet
        workbook.removeSheetAt(tempSheetNo);
    }

    /**
     * 移除掉行区域
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param startRow 起始行
     * @param endRow 结束行
     * */
    public synchronized void removeRowArea(int sheetNo,int startRow,int endRow){
        if(!examine() || !initSheet(sheetNo) || startRow > endRow)
            return;

        // 复制当前需要操作的sheet到一个临时的sheet
        Sheet tempSheet = workbook.cloneSheet(sheetNo);
        // 获取临时sheet在workbook当中的索引
        int tempSheetNo = workbook.getSheetIndex(tempSheet);
        // 得到临时sheet的第一个row的索引
        int firstRowNum = tempSheet.getFirstRowNum();
        // 得到临时sheet的最后一个row的索引
        int lastRowNum = tempSheet.getLastRowNum();
        // 清空sheet
        if(!clearSheet(sheetNo)){
            return;
        }

        int delNum = endRow - startRow + 1;
        for(int i = firstRowNum;i <= lastRowNum;i++){
            Row fromRow = tempSheet.getRow(i);
            Row toRow =  sheet.createRow(i);
            if(i < startRow)
                copyRow(tempSheetNo,fromRow,sheetNo,toRow,true,false);
            else
                copyRow(tempSheetNo,tempSheet.getRow(i + delNum),sheetNo,toRow,true,false);
        }
        List<CellRangeAddress> crds = tempSheet.getMergedRegions();
        if(crds == null)
            return;
        crds.forEach(crd -> {
            if(crd != null){
                int firstMergedRow = crd.getFirstRow();
                int lastMergedRow = crd.getLastRow();
                int firstMergedColumn = crd.getFirstColumn();
                int lastMergedClolunm = crd.getLastColumn();
                if(lastMergedRow < startRow)
                    safeMergedRegion(sheetNo,crd);
                else if(lastMergedRow >= startRow){
                    if(lastMergedRow <= endRow){
                        if(firstMergedRow < startRow){
                            mergedRegion(sheetNo,firstMergedRow,startRow - 1,firstMergedColumn,lastMergedClolunm);
                        }
                    }
                    else if(lastMergedRow > endRow){
                        if(firstMergedRow < startRow){
                            mergedRegion(sheetNo,firstMergedRow,lastMergedRow - delNum,firstMergedColumn,lastMergedClolunm);
                        }
                        else if(firstMergedRow >= startRow && firstMergedRow <= endRow){
                            mergedRegion(sheetNo,endRow + 1 - delNum,lastMergedRow - delNum,firstMergedColumn,lastMergedClolunm);
                        }
                        else if(firstMergedRow > endRow){
                            mergedRegion(sheetNo,firstMergedRow - delNum,lastMergedRow - delNum,firstMergedColumn,lastMergedClolunm);
                        }
                    }
                }
            }
        });
        settingColumnWidth(tempSheetNo,sheetNo);
        // 删除临时的sheet
        workbook.removeSheetAt(tempSheetNo);
    }

    /**
     * 移除掉列区域
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @param startCol 起始列
     * @param endCol 结束列
     * */
    public synchronized void removeColumnArea(int sheetNo,int startCol,int endCol){
        if(!examine() || !initSheet(sheetNo) || startCol > endCol)
            return;

        // 复制当前需要操作的sheet到一个临时的sheet
        Sheet tempSheet = workbook.cloneSheet(sheetNo);
        // 获取临时sheet在workbook当中的索引
        int tempSheetNo = workbook.getSheetIndex(tempSheet);
        // 得到临时sheet的第一个row的索引
        int firstRowNum = tempSheet.getFirstRowNum();
        // 得到临时sheet的最后一个row的索引
        int lastRowNum = tempSheet.getLastRowNum();

        if(!clearSheet(sheetNo)){
            return;
        }

        for(int i = firstRowNum;i <= lastRowNum;i++){
            Row row = tempSheet.getRow(i);
            if(row != null){
                for(int j = 0;j < row.getLastCellNum();j++){
                    // 到达删除区间之前正常复制
                    if(j < startCol)
                        copyColumn(tempSheetNo,j,sheetNo,j,true);
                        // 到达删除区间后，跳过区间长度复制
                    else
                        copyColumn(tempSheetNo,j + endCol - startCol + 1,sheetNo,j,true);
                }
            }
        }
        List<CellRangeAddress> crds = tempSheet.getMergedRegions();
        if(crds == null)
            return;
        crds.forEach(crd -> {
            int delColNum = endCol - startCol + 1;
            int firstMergedRow = crd.getFirstRow();
            int lastMergedRow = crd.getLastRow();
            int firstMergedColumn = crd.getFirstColumn();
            int lastMergedClolunm = crd.getLastColumn();
            if(lastMergedClolunm < startCol)
                safeMergedRegion(sheetNo,crd);
            else if(lastMergedClolunm >= startCol){
                if(lastMergedClolunm <= endCol){
                    if(firstMergedColumn < startCol){
                        mergedRegion(sheetNo,firstMergedRow,lastMergedRow,firstMergedColumn,startCol - 1);
                    }
                }
                else if(lastMergedClolunm > endCol){
                    if(firstMergedColumn < startCol){
                        mergedRegion(sheetNo,firstMergedRow,lastMergedRow,firstMergedColumn,lastMergedClolunm - delColNum);
                    }
                    else if(firstMergedColumn >= startCol && firstMergedColumn <= endCol){
                        mergedRegion(sheetNo,firstMergedRow,lastMergedRow,endCol + 1 - delColNum,lastMergedClolunm - delColNum);
                    }
                    else if(firstMergedColumn > endCol){
                        mergedRegion(sheetNo,firstMergedRow,lastMergedRow,firstMergedColumn - delColNum,lastMergedClolunm -delColNum);
                    }
                }
            }
        });
        // 删除临时的sheet
        workbook.removeSheetAt(tempSheetNo);
    }

    private void settingColumnWidth(int sourceSheetNo,int sheetNo){
        if(sourceSheetNo < 0 || sourceSheetNo > workbook.getNumberOfSheets() ||
                sheetNo < 0 || sheetNo > workbook.getNumberOfSheets())
            return;
        List<Row> rows = new ArrayList<>();
        for(int i = sheet.getFirstRowNum();i <= sheet.getLastRowNum();i++){
            Row row = sheet.getRow(i);
            if(row != null)
                rows.add(row);
        }
        Row maxColumnRow = rows.stream().max((r1,r2) -> {
            if (r1 == null && r2 == null) {
                return 0;
            }
            if (r1 == null) {
                return 1;
            }
            if (r2 == null) {
                return -1;
            }
            if (r1.getLastCellNum() == r2.getLastCellNum())
                return 0;
            if (r1.getLastCellNum() > r2.getLastCellNum())
                return 1;
            else
                return -1;
        }).filter(r -> r != null).orElse(null);
        if(maxColumnRow != null){
            int maxColumn = maxColumnRow.getLastCellNum();
            for (int i = 0; i < maxColumn; i++) {
                workbook.getSheetAt(sheetNo).setColumnWidth(i,workbook.getSheetAt(sourceSheetNo).getColumnWidth(i));
            }
        }
    }

    /**
     * 清除掉sheet，清除不是删除，只是会清除所有
     * 的列的值和和合并单元格
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @return boolean true-成功 false-失败
     * */
    public synchronized boolean clearSheet(int sheetNo){
        if(!examine())
            return false;
        int sheetNum;
        if(sheetNo < 0 || sheetNo > (sheetNum = workbook.getNumberOfSheets()))
            return false;

        for(int i = 0;i < sheetNum;i++){
            if(i == sheetNo){
                String sheetName = workbook.getSheetName(i);
                workbook.removeSheetAt(i);
                workbook.createSheet(sheetName);
            }
            if(i > sheetNo){
                int offset = i - sheetNo;
                String sheetName = workbook.getSheetName(i-offset);
                Sheet newSheet = workbook.cloneSheet(i-offset);
                workbook.removeSheetAt(i-offset);
                workbook.setSheetName(workbook.getSheetIndex(newSheet),sheetName);
            }
        }
        if(!initSheet(sheetNo))
            return false;
        return true;
    }

    /**
     * 存储Excel
     *
     * @param path 存储路径
     * @throws IOException
     */
    public void save(String path) throws
            IOException {
        exception();
        if(!examine())
            return;
        try (FileOutputStream fos = new FileOutputStream(path)){
            workbook.write(fos) ;
        }
    }

    /**
     * 返回Excel的字节数组
     *
     * @return byte[]
     */
    public byte[] getBytes(){
        if(!examine())
            return null;
        try(ByteArrayOutputStream ops = new ByteArrayOutputStream()){
            workbook.write(ops);
            return ops.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回Workbook
     *
     * @return Workbook
     * @throws IOException
     * */
    public Workbook getWorkbook()
            throws IOException {
        exception();
        return workbook;
    }


    /**
     *
     * @param sheetNum 需读取内容的sheet 第几页数
     * @return
     * @throws IOException
     */
    public List<List<String>> readExcel(int sheetNum)
            throws IOException {
        if(!examine())
            return null;
        int numberOfSheets = workbook.getNumberOfSheets();
        if (sheetNum >= numberOfSheets) {
            throw new IllegalArgumentException("超过sheet范围，无法读取");
        }
        List<List<String>> list=new ArrayList<List<String>>();
            //读取Sheet
        Sheet sheet=workbook.getSheetAt(sheetNum);
        if(sheet==null){
            return Collections.EMPTY_LIST;
        }
        //处理当前页，循环每一行
        for (int j = 0; j < sheet.getPhysicalNumberOfRows(); j++) {
            //得到当前行
            Row row=sheet.getRow(j);
            //当前行第一个单元格
            int minCells=row.getFirstCellNum();
            //当前行最后一个单元格
            int maxCells=row.getLastCellNum();
            List<String> sl=new ArrayList<String>();
            for (int k = minCells; k < maxCells; k++) {
                //每一个单元格
                Cell cell=row.getCell(k);
                if(cell==null){
                    continue;
                }
                sl.add(cell.toString());
            }
            list.add(sl);
        }
        return list;
    }

    /**
     * @return sheet的数量
     * */
    public int getSheetNum(){
        return workbook.getNumberOfSheets();
    }

    /**
     * 返回sheet的行数量
     *
     * @param sheetNo 需要操作的Sheet的编号
     * @return int 行数量
     * */
    public int getSheetRowNum(int sheetNo){
        if(!examine() || !initSheet(sheetNo))
            return 0;
        return sheets[sheetNo].getLastRowNum();
    }

    /**
     * 设置excel的缩放率
     *
     * @param zoom 缩放率
     * */
    public void setZoom(int zoom){
        if(!examine() || !initSheet(workbook.getSheetIndex(sheet)))
            return;
        for (int i = 0; i < sheets.length; i++) {
            sheets[i].setZoom(zoom);
        }
    }

    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;
        if(o == this)
            return true;
        if(!(o instanceof ExcelTemplate))
            return false;
        if(examine() ^ ((ExcelTemplate)o).examine())
            return false;
        return Objects.equals(path,((ExcelTemplate)o).path);
    }

    @Override
    public int hashCode(){
        int hash = Objects.hashCode(path);
        return hash >>> 16 ^ hash;
    }

    @Override
    public String toString(){
        return "ExcelTemplate from " + path + " is " +
                (examine() ? "effective" : "invalid");
    }


    /**
     * 根据列个数创建excel 行列及列边框
     * @param cellNum 列个数
     */
    public void createCellAndBorderByNum(Row row, int cellNum) throws IOException {
        CellStyle cellStyle = getWorkbook().createCellStyle(); //创建一个样式
        cellStyle.setBorderBottom(BorderStyle.THIN); //底部边框
        cellStyle.setBorderLeft(BorderStyle.THIN); //左边框
        cellStyle.setBorderRight(BorderStyle.THIN);//右边框
        cellStyle.setBorderTop(BorderStyle.THIN);//顶部边框
        for(int i=0;i<cellNum;i++){
            if(i>0){
                row.createCell(i).setCellStyle(cellStyle);
            }else {
                row.createCell(i);
            }
        }
    }
    /**
     * 根据列个数创建excel 行列
     * @param cellNum 列个数
     */
    public void createCellByNum(Row row, int cellNum) throws IOException {
        for(int i=0;i<cellNum;i++){
            row.createCell(i);
        }
    }
}

