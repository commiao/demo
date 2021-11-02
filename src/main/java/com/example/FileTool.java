package com.example;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileTool {
    public static List<String> getFiles(String path) {
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                files.add(tempList[i].toString());
                //文件名，不包含路径
                //String fileName = tempList[i].getName();
            }
            if (tempList[i].isDirectory()) {
                //这里就不递归了，
                files.addAll(getFiles(tempList[i].toString()));
            }
        }
        return files;
    }

    private static String getFileName(File file) {
        String filename = file.getName();
        if (filename.indexOf(".") >= 0) {
            filename = filename.substring(0, filename.lastIndexOf("."));
        }
        return filename;
    }

    private static String getFileSuffix(File file) {
        String filename = file.getName();
        String suffix = null;
        if (filename.indexOf(".") >= 0) {
            suffix = filename.substring(filename.lastIndexOf("."));
        }
        return suffix;
    }

    public static boolean rename(String oldFilePath, String newName, String newSuffix, String newDirectoryPath) {
        File file = new File(oldFilePath); //指定文件名及路径
        // 未指定新文件名 则取旧的文件名
        String filename = StringUtils.isNotBlank(newName) ? newName : getFileName(file);
        // 未指定后缀名 则取旧的后缀名
        String suffix = StringUtils.isNotBlank(newSuffix) ? newSuffix : getFileSuffix(file);
        // 如果无后缀名 则直接以文件名命名
        if (suffix == null) {
            suffix = "";
        }
        String parent = StringUtils.isNotBlank(newDirectoryPath) ? newDirectoryPath : file.getParent();
        String newFilePath = parent + File.separator + filename + suffix;
        return file.renameTo(new File(newFilePath)); //改名
    }

    public static void main(String[] args) {
        List<String> files = getFiles("F:\\data\\2");
        String fileName = null;
        for (String filePath : files) {
            File file = new File(filePath);
            fileName = getFileName(file);
            String[] str = fileName.split("_");
            rename(file.getAbsolutePath(), str[0], "." + str[1], "F:\\data\\2");
        }
    }
}
