package com.lxm.framework.excel;
import com.lxm.framework.excel.util.ExcelUtils;
import com.lxm.framework.excel.entity.*;
import com.lxm.framework.excel.annotation.Excel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ExcelRegressionTest {
 public static class Entry {
  @Excel(name="姓名") private String name;
  public Entry() {} public Entry(String name){this.name=name;}
  public String getName(){return name;} public void setName(String value){name=value;}
 }
 @Test void readsLastRowMissingCellsAndDetectedFormat() throws Exception {
  byte[] bytes;
  try(var workbook=new XSSFWorkbook();var output=new ByteArrayOutputStream()) {
   var sheet=workbook.createSheet("test");sheet.createRow(0).createCell(0).setCellValue("header");
   sheet.createRow(1).createCell(0).setCellValue("first");sheet.createRow(3).createCell(1).setCellValue("last");workbook.write(output);bytes=output.toByteArray();
  }
  var result=ExcelUtils.read(new MockMultipartFile("file","incorrect.xls","application/octet-stream",bytes),0,2,true);
  assertEquals(3,result.size());assertEquals(List.of("","last"),result.getLast());assertEquals(List.of("",""),result.get(1));
 }
 @Test void multiSheetExportKeepsAllSheetsAndLiteralStrings() throws Exception {
  var sheets=new ExcelMultiSheets();for(int i=0;i<20;i++) sheets.appendSheet("sheet"+i,Entry.class,List.of(new Entry("=1+1")));
  try(var workbook=ExcelUtils.exportMultiSheets(sheets);var output=new ByteArrayOutputStream()) {
   assertEquals(20,workbook.getNumberOfSheets());workbook.write(output);
   try(var read=WorkbookFactory.create(new ByteArrayInputStream(output.toByteArray()))) {
    assertEquals(20,read.getNumberOfSheets());boolean found=false;
    for(var sheet:read) for(var row:sheet) for(var cell:row) if(cell.toString().equals("=1+1")){assertEquals(CellType.STRING,cell.getCellType());found=true;}
    assertTrue(found);
   }
  }
 }
}
