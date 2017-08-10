package com.gionee.demo;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import jxl.Workbook;
import jxl.biff.NumFormatRecordsException;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Border;
import jxl.write.BorderLineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;

public class ExeclUtil {
	public static String[] TAB = { "时间", "电压(V)", "温度(℃)", "电量(%)", "空闲RAM",
			"空闲ROM", "空闲内部存储空间或SD" };
	public static int[] TAB1 = { 20, 10, 10, 10, 10, 10, 25 };
	public static String PATH = Environment.getExternalStorageDirectory()
			+ "/AutoGionee/";
	WritableWorkbook nWritableWorkbook = null;
	WritableSheet nWritableSheet = null;

	/**
	 * 新建一个xls文件，无文件时新建，有文件追加
	 * 
	 * @param path
	 * @return
	 */
	public WritableWorkbook creatExl(String path) {
		File fileName = new File(path + ".xls");
		try {
			Log.i(Util.TAG, "SDPATH:" + fileName.getPath());
			try {
				// 要追加新记录的
				Workbook originalWbook = Workbook.getWorkbook(fileName);
				nWritableWorkbook = Workbook.createWorkbook(fileName,
						originalWbook);
			} catch (Throwable e) {
				// TODO: handle exception
				Log.e(Util.TAG, "error:" + e.getLocalizedMessage());
				nWritableWorkbook = Workbook.createWorkbook(fileName);
			}
			// nWritableWorkbook = Workbook.createWorkbook(fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nWritableWorkbook;
	}

	/**
	 * 新建ecexl页
	 * 
	 * @param sheetName
	 * @param Tab
	 */
	public void creatSheet(String sheetName, String[] Tab, int[] fontsize,
			int sheets) {
		boolean isExist = true;
		try {
			try {
				Log.i(Util.TAG, sheets + "sheetName:"
						+ nWritableWorkbook.getSheet(sheets).getName());
			} catch (Exception e) {
				// TODO: handle exception
				isExist = false;
				Log.e(Util.TAG, "sheet:" + e.getLocalizedMessage());
			}
			Log.i(Util.TAG, "file isExist:" + isExist);
			if (isExist) {
				nWritableSheet = nWritableWorkbook.getSheet(sheets);
			} else {
				nWritableSheet = nWritableWorkbook.createSheet(sheetName,
						sheets); // 生成的sheet名称
				for (int i = 0; i < Tab.length; i++) {
					// 定义每一列的宽度
					nWritableSheet.setColumnView(i, fontsize[i]);
					// Label(x,y,z)代表单元格的第x+1列，第y+1行,内容z
					Label label = new Label(i, 0, Tab[i], getTitleFormat());
					// 将定义好的单元格添加到工作表中
					nWritableSheet.addCell(label);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 填入内容
	 * 
	 * @param contian
	 */
	public void writeExl(String[] contian, int sheets) {
		try {
			nWritableSheet = nWritableWorkbook.getSheet(sheets);
			int count = nWritableSheet.getRows();
			for (int i = 0; i < contian.length; i++) {
				// Label(x,y,z)代表单元格的第x+1列，第y+1行,内容z
				Label label = new Label(i, count, contian[i], getNormalFormat());
				// 将定义好的单元格添加到工作表中
				nWritableSheet.addCell(label);

			}
			nWritableWorkbook.write();
			nWritableWorkbook.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/** 用于标题 **/
	public WritableCellFormat getTitleFormat() {
		WritableCellFormat titleFormat = null;
		try {
			WritableFont titleFont = new WritableFont(
					WritableFont.createFont("宋体"), 10, WritableFont.BOLD);
			titleFormat = new WritableCellFormat(titleFont);
			titleFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
			titleFormat.setVerticalAlignment(VerticalAlignment.CENTRE); // 垂直对齐
			titleFormat.setAlignment(Alignment.LEFT); // 水平对齐
			// titleFormat.setWrap(true); // 是否换行
			titleFormat.setBackground(Colour.GRAY_25);// 背景色暗灰-25%
		} catch (Exception e) {
			// TODO: handle exception
		}
		return titleFormat;
	}

	/** 用于正文 **/
	public WritableCellFormat getNormalFormat() {
		WritableCellFormat normalFormat = null;
		try {
			WritableFont normalFont = new WritableFont(
					WritableFont.createFont("宋体"), 10);
			normalFont.setColour(Colour.GREEN);
			normalFormat = new WritableCellFormat(normalFont);
			normalFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
			normalFormat.setVerticalAlignment(VerticalAlignment.CENTRE); // 垂直对齐
			normalFormat.setAlignment(Alignment.LEFT);// 水平对齐
			normalFormat.setBackground(Colour.GRAY_25);

		} catch (Exception e) {
			// TODO: handle exception
		}

		return normalFormat;
	}

}