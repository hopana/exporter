package com.saber.tables;

import com.saber.tables.utils.DbUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Saber on 2018/5/28.
 */
public class Table {

    private static DbUtils dbUtils = DbUtils.getInstance();

    /**
     * excel导出路径为当前项目目录下
     */
    public final static String excelFilePath = System.getProperty("user.dir");

    /**
     * 获取用户下所有的表
     *
     * @return List<String>
     */
    public static List<String> getTables(){

        List<String> list = new ArrayList<String>();

        List<String> tableNameList = dbUtils.getTableNameList();
        for (String s : tableNameList) {

            //过滤掉分表 如：20180406   2018040612
            String regx = "\\d{4}\\d{2}\\d{2}([0-9]{0,2})$";
            Pattern p = Pattern.compile(regx);
            Matcher matcher = p.matcher(s);
            //过滤掉分表和_BAK结尾的备份表 如：20180406   2018040612
            if(!matcher.find() && !s.endsWith("_BAK")){
                list.add(s);
            }
        }

        return list;
    }

    /**
     * 获取表字段
     *
     * @param tableName 表名
     * @return List<String>
     */
    public static List<String> getColumns(String tableName){

        List<String> columnNameList = dbUtils.getColumnNameList(tableName);
        for (String colnum : columnNameList) {
            System.out.println("colnum = " + colnum);
        }
        return columnNameList;
    }

    /**
     * 获取表结构
     *
     * @param tableName 表名
     * @return List<TableEntity>
     */
    public static List<TableEntity> getStructOfTable(String tableName){
        List<TableEntity> columnNameList = dbUtils.getStructOfTable(tableName);
        for (TableEntity entity : columnNameList) {
            System.out.println("entity = " + entity);
        }
        return columnNameList;
    }

    /**
     * 转换成excel
     *
     * @param map           Map<String,List<TableEntity>>   key为表名，List<TableEntity>为字段信息
     * @param filePath      要保存的文件路径
     */
    public static void data2Excel(Map<String,List<TableEntity>> map,String filePath){
        FileOutputStream fos = null;

        HSSFRow row = null;
        HSSFCell cell = null;
        HSSFCellStyle style = null;
        HSSFFont font = null;


        HSSFWorkbook wb = null;

        String[] tableFiled = {"COLUMN_NAME", "DATA_TYPE", "NULLABLE", "DATA_DEFAULT", "COLUMN_ID","COMMENTS"};

        try {
            wb = new HSSFWorkbook();

            fos = new FileOutputStream(filePath);
            //用于标注sheet表名下标
            int sheetIndex = 0;

            //遍历表
            for (String tableName : map.keySet()) {

                int currentRowNum = 0;

                //是否隐藏，当表注释和字段注释都有时隐藏该sheet
                boolean isHide = true;

                //创建新的sheet并设置名称
                try {

                    //创建sheet
                    HSSFSheet sheet = wb.createSheet();

                    //以表名做sheet名
                    wb.setSheetName(sheetIndex, tableName);
                    sheetIndex++;

                    style = wb.createCellStyle();
                    font = wb.createFont();

                    //新建一行,再在行上面新建一列
                    row = sheet.createRow(currentRowNum);
                    currentRowNum++;

                    //创建标题
                    for (int i = 0; i < tableFiled.length; i++) {
                        cell = row.createCell((short) i);
                        cell.setCellValue(tableFiled[i]);

                        //设置样式
                        font.setBold(true);   //字体加粗
                        font.setColor(Font.COLOR_NORMAL);
                        font.setUnderline(Font.U_SINGLE);
                        style.setFont(font);
                        style.setAlignment(HorizontalAlignment.CENTER);//水平居中
                        style.setFillForegroundColor((short) 13);// 设置背景色
                        style.setFillPattern(FillPatternType.FINE_DOTS);
                        style.setBorderBottom(BorderStyle.THIN); //下边框
                        style.setBorderLeft(BorderStyle.THIN);//左边框
                        style.setBorderTop(BorderStyle.THIN);//上边框
                        style.setBorderRight(BorderStyle.THIN);//右边框
                        cell.setCellStyle(style);
                    }

                    //=============         遍历表字段信息         =============//

                    //获取单张表表字段信息
                    List<TableEntity> list = map.get(tableName);
                    //计数
                    int COLUMN_ID = 1;
                    //遍历字段信息
                    for (TableEntity entity : list) {

                        //创建行（从第二行开始）
                        row = sheet.createRow(currentRowNum);
                        currentRowNum++;

                        //创建列

                        //字段名
                        cell = row.createCell((short) 0);
                        cell.setCellValue(new HSSFRichTextString(entity.getColumnName()));
                        sheet.setColumnWidth((short) 0, (short) 8000);

                        //字段类型
                        cell = row.createCell((short) 1);
                        StringBuilder colnumNName = new StringBuilder(entity.getDataType());
                        String excludeType = "DATE,Timestamp,int,long.INTEGER,LONG";
                        if(!excludeType.contains(entity.getDataType())){
                            colnumNName.append("(");
                            if(entity.getDataLength() != null){
                                colnumNName.append(entity.getDataLength());
                            }
                            if (entity.getDataPrecision() != null){
                                colnumNName.append(entity.getDataPrecision());
                            }
                            if(entity.getDataScale() != null){
                                colnumNName.append(",").append(entity.getDataScale());
                            }
                            colnumNName.append(")");
                        }
                        cell.setCellValue(new HSSFRichTextString(colnumNName.toString()));
                        sheet.setColumnWidth((short) 1, (short) 5000);

                        //是否为空
                        cell = row.createCell((short) 2);
                        String nullable;
                        if(entity.getNullable().equalsIgnoreCase("Y")){
                            nullable = "Yes";
                        }else {
                            nullable = "No";
                        }
                        cell.setCellValue(new HSSFRichTextString(nullable));
                        sheet.setColumnWidth((short) 2, (short) 2500);

                        //默认值
                        cell = row.createCell((short) 3);
                        String dataDefault = entity.getDataDefault();
                        if(dataDefault == null) dataDefault = "null";
                        cell.setCellValue(new HSSFRichTextString(dataDefault));
                        sheet.setColumnWidth((short) 3, (short) 2500);

                        //COLUMN_ID
                        cell = row.createCell((short) 4);
                        cell.setCellValue(String.valueOf(COLUMN_ID));
                        //设置样式 右对齐
                        CellStyle cs_colnumId = wb.createCellStyle();
                        cs_colnumId.setAlignment(HorizontalAlignment.RIGHT);
                        cell.setCellStyle(cs_colnumId);
                        //设置宽度
                        sheet.setColumnWidth((short) 4, (short) 2500);
                        COLUMN_ID++;

                        //注释
                        cell = row.createCell((short) 5);
                        String comments = entity.getComments();
                        if("\n".equals(comments) || (comments != null && comments.length() == 0)){
                            comments = null;
                        }
                        cell.setCellValue(new HSSFRichTextString(comments));
                        sheet.setColumnWidth((short) 5, (short) 15000);
                        //如果为空，设置背景色
                        if(comments == null){

                            //不隐藏
                            isHide = false;

                            HSSFPalette palette = wb.getCustomPalette(); //wb HSSFWorkbook对象
                            palette.setColorAtIndex((short) 9, (byte) (197), (byte) (230), (byte) (241));

                            CellStyle cellStyle = wb.createCellStyle();
                            cellStyle.setFillPattern(FillPatternType.FINE_DOTS);//设置前景填充样式 纯色填充
                            cellStyle.setFillForegroundColor((short) 9);//前景填充色
                            //设置自动换行
                            cellStyle.setWrapText(true);
                            cell.setCellStyle(cellStyle);
                        }
                    }

                    //表注释
                    String tableComment = list.get(0).getTableComment();
                    row = sheet.createRow(currentRowNum+4);
                    cell = row.createCell(0);
                    cell.setCellValue(new HSSFRichTextString("表COMMENT"));

                    cell = row.createCell(1);
                    cell.setCellValue(new HSSFRichTextString(tableComment));
                    sheet.setColumnWidth(currentRowNum+4, (short) 15000);
                    //如果为空，设置背景色
                    if(tableComment == null){

                        //不隐藏
                        isHide = false;

                        HSSFPalette palette = wb.getCustomPalette(); //wb HSSFWorkbook对象
                        palette.setColorAtIndex((short) 9, (byte) (220), (byte) (230), (byte) (241));

                        CellStyle cellStyle = wb.createCellStyle();
                        cellStyle.setFillPattern(FillPatternType.FINE_DOTS);//设置前景填充样式 纯色填充
                        cellStyle.setFillForegroundColor((short) 9);//前景填充色
                        //设置自动换行
                        cellStyle.setWrapText(true);
                        cell.setCellStyle(cellStyle);
                    }

                }catch (Exception e){
                   e.printStackTrace();
                }

                //设置sheet显隐
                wb.setSheetHidden((sheetIndex-1),isHide);//这里减1是因为这个sheet还没创建
            }
            wb.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(filePath+"文件创建失败！");

        } finally {
           // assert fos != null;
            try {
                fos.close();
                wb.close();
            }catch (Exception e){}
        }
    }


    public static void main(String[] args) {

        Map<String,List<TableEntity>> map = new HashMap<String,List<TableEntity>>(100);

        List<String> tables = getTables();
        System.out.println("tables.size() = " + tables.size());
        for (String table : tables) {
            System.out.println("table = " + table);

            map.put(table,getStructOfTable(table));
        }
        String filePath = excelFilePath + File.separator+DbUtils.fileName;
        data2Excel(map,filePath);
    }
}
