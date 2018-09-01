package com.leo.rms.utils;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
 








import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

 
 
 
/**
 * 数据导出到excel
 * 
 * @author wujx
 * @since 1.0, Sep 12, 2012
 */
public class ExcelUtil {
    /**
     * 设置或获取工作本显示的名称
     */
     private String sheetName;
     /**
      * 设置或获取标题
      */
     private String title;
     /**
      * 设置或获取页眉
      */
     private String[][] header = null;
     /**
      * 设置或获取页脚
      */
     private String[][] footer = null;
     /**
      * 是否隐藏打印时间
      */
     private boolean hideDate = false;
     /**
      * 用于分页的最大记录数
      */
     private int recordNum = 10000;
      
      
     public String getSheetName() {
        return sheetName;
    }
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String[][] getHeader() {
        return header;
    }
    public void setHeader(String[][] header) {
        this.header = header;
    }
    public String[][] getFooter() {
        return footer;
    }
    public void setFooter(String[][] footer) {
        this.footer = footer;
    }
    public boolean isHideDate() {
        return hideDate;
    }
    public void setHideDate(boolean hideDate) {
        this.hideDate = hideDate;
    }
    public int getRecordNum() {
        return recordNum;
    }
    public void setRecordNum(int recordNum) {
        this.recordNum = recordNum;
    }
     
    /**
     * 导出数据到Excel
     * @param dataSource 数据源
     * @param filePath 导出的路径
     * @return
     */
    public boolean toExcel(List<Map<String,Object>> dataSource,OutputStream out)  {
        if (dataSource == null)
            return false;
        HSSFWorkbook book = new HSSFWorkbook();  //创建一个工作簿
        
        int rCount = dataSource.size() > 0 ? dataSource.size()-1 : 0;  //行数
        int cCount = dataSource.size() == 0 ? 0 : dataSource.get(0).size();//列数
 
        int sheetCount = getSheetCount(rCount);//获取分页工作表的个数
        if (sheetCount > 1){
            for (int i = 1; i <= sheetCount; i++){//初始化工作表的个数
                book.createSheet(isNullOrEmpty(sheetName)? "Sheet-" + i : sheetName + "-" + i);  //添加一个工作表
            }
        } else {
            book.createSheet(isNullOrEmpty(sheetName)? "Sheet": sheetName );  //添加一个工作表
        }
        
        //生成标题样式
        HSSFCellStyle titleStyle=book.createCellStyle();
        //设置这些样式
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//标题居中对齐 
        titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中  
        titleStyle.setWrapText(true);//自动换行
        //生成一个字体
        HSSFFont font=book.createFont();
        font.setFontHeightInPoints((short)16);//标题字体大小
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//标题加粗
        //把字体应用到当前的样式
        titleStyle.setFont(font);
         
        //生成表头样式
        HSSFCellStyle headStyle = book.createCellStyle();
        headStyle.cloneStyleFrom(titleStyle);
        headStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        headStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        headStyle.setWrapText(false);//取消自动换行
        font.setFontName("微软雅黑");
        font.setBoldweight(Short.valueOf("0"));
        font = book.createFont();
        font.setFontHeightInPoints((short)14); 
        //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headStyle.setFont(font);    
         
        //生成内容样式
        HSSFCellStyle bodyStyle=book.createCellStyle();
        bodyStyle.cloneStyleFrom(headStyle);
        bodyStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        bodyStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        font=book.createFont();
        font.setFontHeightInPoints((short)12);
        font.setBoldweight(Short.valueOf("0"));
        font.setFontName("宋体");
        bodyStyle.setFont(font);
         
        for (int index = 0, rowIndex = 1; index < sheetCount; index++) {
            int rows = getRows(rCount, index + 1);//获取每页的记录数 
            HSSFSheet sheet = book.getSheetAt(index);//获取工作簿的第一个工作表
            HSSFRow row = null;
            HSSFCell cell = null;
            Object value = null;
            int frontRow = 0;//前置行数
 
            //设置页眉和标题
             
            //设置标题
            if (!isNullOrEmpty(title)) {
                sheet.addMergedRegion(new CellRangeAddress(frontRow,0,frontRow,cCount-1));//合并标题行
                row = sheet.createRow(frontRow);
                cell = row.createCell(0);
                row.setHeightInPoints(50);//设置行高                
                cell.setCellStyle(titleStyle);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING); 
                cell.setCellValue(title);
                frontRow++;
            }
             
            //页眉设置
            if (header != null) {
                int Rows = header[0].length;
                int Fields = header[1].length;
                for (int i = 0; i < Rows; i++) {
                    row = sheet.createRow(frontRow);
                    row.setHeightInPoints(23);//设置行高
                    for (int j = 0; j < Fields; j++) {
                        cell = row.createCell(j);
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING); 
                        cell.setCellValue(header[i][j]);//设置数据部分                       
                        //cell.Style.Font.Size = 11;//设置字体大小
                    }
                    frontRow++;
                }
                frontRow++;
            }
             
            //数据写入
            if(dataSource.size() > 0 && cCount >0){
                //导出表头部分
                row = sheet.createRow(frontRow);
                row.setHeightInPoints(25);//设置行高
                int tally = 0;//临时计数器
                for (Entry<String,Object> entry : dataSource.get(0).entrySet()) {
                    cell = row.createCell(tally);   
                    cell.setCellStyle(headStyle);
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING); 
                    value = entry.getValue() == null ? "" : entry.getValue() + "";
                    cell.setCellValue(value + "");//设置表头
                    sheet.setColumnWidth(tally,(value+"").getBytes().length * 300);
                    //sheet.autoSizeColumn(dataSource.size(), true); 
                    tally++;
                }
                frontRow++;
                 
                // 导出数据部分
                for (int i = 0; i < rows; i++) {
                    row = sheet.createRow(frontRow);
                    row.setHeightInPoints(25);//设置行高
                    tally = 0;
                    for (Entry<String,Object> entry : dataSource.get(0).entrySet()) {
                        value = dataSource.get(rowIndex).get(entry.getKey());
                        cell = row.createCell(tally);                        
                        cell.setCellStyle(bodyStyle);
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING); 
                        //设置数据部分 
                        if (value!=null){
                            if(entry.getValue() instanceof Boolean)
                                cell.setCellValue((Boolean) value ? "是" : "否");
                            if(entry.getValue() instanceof Date)
                                cell.setCellValue(value+"");//DateUtils.format((Date) entry.getValue(), "yyyy-MM-dd"));
                            else
                                cell.setCellValue(value==null?"":value+"");
                        }
                        tally++;
                    }
                    frontRow++;
                    rowIndex++;
                }
                frontRow++;
            }
 
            //设置页脚
            //设置页脚数据
            if (footer != null) {
                int Rows = footer[0].length;
                int Fields = footer[1].length;
                for (int i = 0; i < Rows; i++) {
                    row = sheet.createRow(frontRow);
                    row.setHeightInPoints(17);//设置行高
                    for (int j = 0; j < Fields; j++) {
                        cell = row.createCell(j);
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING); 
                        cell.setCellValue(footer[i][j]);//设置数据部分                       
                        //cell.Style.Font.Size = 11;//设置字体大小
                    }
                    frontRow++;
                }
            }
 
            if (!hideDate){//时间落款                
                row = sheet.createRow(frontRow);
                row.setHeightInPoints(17);//设置行高
                cell = row.createCell(cCount > 1 ? cCount - 2 : cCount);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING); 
                //cell.setCellValue("制表时间：" + DateUtils.getCurrDateStr());//设置数据部分       
                //cell.Style.Font.Size = 11;//设置字体大小
                frontRow++;
            }
 
            if (sheetCount > 1) {
                row = sheet.createRow(frontRow);
                row.setHeightInPoints(17);//设置行高
                cell = row.createCell(cCount > 1 ? cCount - 2 : cCount);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                cell.setCellValue("<共" + sheetCount + "页，第" + (index + 1) + "页>");//设置数据部分       
                //cell.Style.Font.Size = 11;//设置字体大小                    
            }
            for(int i=0; i<cCount; i++){
              sheet.autoSizeColumn(i);//自动分布列宽
              sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
        }
        try  {
//            filePath = filePath.replace("/", File.separator).replace("\\\\", File.separator);
//            File destDir = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
//            if (!destDir.exists()) {
//                destDir.mkdirs();
//            }
//            FileOutputStream fileOut = new FileOutputStream(filePath);
//            book.write(fileOut);
//            fileOut.close();
            
            
            book.write(out);
			out.flush();
			out.close();
            return true;
         } catch (Exception e) {
            e.printStackTrace();
        }finally{
             
        }
        return false;
    }
         
     
    /**
     * 确定分页的个数
     * @param rCount 总得记录条数
     * @return
     */
    private int getSheetCount(int rCount){
        if (recordNum <= 0)
            return 1;
        if (rCount <= 0) return 1;
        int n = rCount % recordNum; //余数
        if (n == 0) {
            return rCount / recordNum;
        } else {
            return (int)(rCount / recordNum) + 1;
        }
    }
     
    /**
     * 确定每页的记录数
     * @param rCount 总得记录条数
     * @param page 当前页码
     * @return
     */
    private int getRows(int rCount, int page){
        if (recordNum <= 0)
            return rCount;
 
        if (rCount - page * recordNum >= 0)
            return recordNum;
        else
            return rCount % recordNum; //余数
    }
     
    private boolean isNullOrEmpty(Object value){
        if (value == null || value.toString().length() == 0) {
            return true;
        }
        return false;
    }
    
    
    public static void main(String[] args,HttpServletResponse response) {
    	try {
	    	ExcelUtil export = new ExcelUtil();
	    	List<Map<String, Object>> map = new ArrayList<Map<String,Object>>();
	    	String title = "芳草地国际小学学生数据表";
			byte[] b = title.getBytes("GBK");   
			title = new String(b,"8859_1");
			response.setHeader("Content-Disposition","attachment;filename="+title + ".xls");
			response.setContentType("application/octet-stream;");//定义输出类型
	    	Map<String, Object> data = new LinkedHashMap<String, Object>();
	    	data.put("1", "单据编号");
	    	data.put("2", "申请单位名称");
	    	data.put("3", "申请部门");
	    	data.put("4", "申请日期");
	    	data.put("5", "申请人");
	    	data.put("6", "所领导审批人");
	    	data.put("7", "审批时间");
	    	map.add(data);
	    	 
	    	for(int i=0;i<2000;i++){
	    	    data = new HashMap<String, Object>();
	    	    data.put("1", 10000);
	    	    data.put("2", "项目名称");
	    	    data.put("3", "试验部门");
	    	    data.put("4", "申请日期");
	    	    data.put("5", "申请人");
	    	    data.put("6", "审批人");
	    	    data.put("7", "审批时间");
	    	    map.add(data);
	    	}
	    	OutputStream os;
			os = response.getOutputStream();
			export.setTitle(title);
	    	export.toExcel(map,os );
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
	}
}