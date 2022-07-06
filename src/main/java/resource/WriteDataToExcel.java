package resource;


// Java program to write data in excel sheet using java code

import java.io.File;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class WriteDataToExcel {
    public String fileName;
    public List<String> headers;
    public List<List<String>> content;

    WriteDataToExcel(String fileName, List<String> headers, List<List<String>> content) {
        this.fileName = fileName;
        this.headers = headers;
        this.content = content;
    }

    // any exceptions need to be caught
    public File run() throws IOException {
        // workbook object
        XSSFWorkbook workbook = new XSSFWorkbook();

        // spreadsheet object
        XSSFSheet spreadsheet
                = workbook.createSheet(" BestBuds Messages ");

        // creating a row object
        XSSFRow row;

        // This data needs to be written (Object[])
        Map<String, Object[]> messageData
                = new TreeMap<String, Object[]>();

        //Header
        messageData.put("1", this.headers.toArray());

        //Content
        int count = 2;
        for (List<String> contentRow : this.content) {
            messageData.put(Integer.toString(count), contentRow.toArray());
            count++;
        }

        Set<String> keyid = messageData.keySet();

        int rowid = 0;

        // writing the data into the sheets...

        for (String key : keyid) {

            row = spreadsheet.createRow(rowid++);
            Object[] objectArr = messageData.get(key);
            int cellid = 0;

            for (Object obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                cell.setCellValue((String)obj);
            }
        }

        // .xlsx is the format for Excel Sheets...
        // writing the workbook into the file...
        java.io.File file = new java.io.File(this.fileName);
        FileOutputStream out = new FileOutputStream(file);

        workbook.write(out);
        out.close();

        return file;
    }
}