package com.ileler.maven.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MavenUtil {
    
    enum Type {
        COPY,
        CLEAN,
        DEPLOY,
    }
    
    public static void main(String[] args) {
        if (args == null || args.length < 1 || args[0] == null) {
            System.err.println("MavenUtil exec error, args is null.");
            return;
        }
        Type type = Type.valueOf(args[0].toUpperCase());
        try {
            type = Type.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
        }
        if (type == null) {
            System.err.println("MavenUtil exec error, type["+args[0]+"] is not found.");
            return;
        }
        try {
            System.out.println("MavenUtil: "+type+" [...]");
            switch (type) {
                case COPY:
                    copy(args);
                    break;
                case CLEAN:
                    clean(args);
                    break;
                case DEPLOY:
                    deploy(args);
                    break;
            }
            System.out.println("MavenUtil: "+type+" [done]");
        } catch (Exception e) {
            System.err.println("MavenUtil exec error: ");
            e.printStackTrace();
        }
    }
    
    private static void deploy (String[] args) throws Exception {
        //deploy
        File baseDir = new File(args[1]);
        File virgoHome = new File(args[2]);
        if (!baseDir.exists() || !baseDir.isDirectory() || !virgoHome.exists() || !virgoHome.isDirectory()) {
            System.err.println("deploy error, args is invalid.");
            return;
        }
        System.out.println("deploy: deploy, baseDir["+args[1]+"], virgoHome["+args[2]+"]");
        File[] files = baseDir.listFiles();
        for (File file : files) {
            if (file.isDirectory() || !file.getName().toLowerCase().endsWith(".plan"))  continue;
            copy(file, new File(virgoHome.getAbsoluteFile() + File.separator + "pickup" + File.separator + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_").format(new Date()) + file.getName()));
            System.out.println("deploy: deploy, copy ["+file.getName()+"] to virgoHome from baseDir");
        }
    }
    
    private static void clean (String[] args) {
        //clean
        File virgoHome = new File(args[1]);
        if (!virgoHome.exists() || !virgoHome.isDirectory()) {
            System.err.println("deploy error, args is invalid.");
            return;
        }
        delList = new ArrayList<>(0);
        System.out.println("deploy: clean, virgoHome["+args[1]+"]");
        File pickup = new File(virgoHome.getAbsoluteFile() + File.separator + "pickup");
        if (pickup.exists()) {
            File[] files = pickup.listFiles();
            for (File file : files) {
                del(file);
            }
        }
        System.out.println("deploy: clean, delete pickup from virgoHome");
        del(new File(virgoHome.getAbsoluteFile() + File.separator + "repository" + File.separator + "usr"));
        System.out.println("deploy: clean, delete repository.usr from virgoHome");
        del(new File(virgoHome.getAbsoluteFile() + File.separator + "work"));
        System.out.println("deploy: clean, delete work from virgoHome");
        del(new File(virgoHome.getAbsoluteFile() + File.separator + "logs"));
        System.out.println("deploy: clean, delete logs from virgoHome");
    }
    
    private static void copy (String[] args) throws Exception {
        //copy
        File src = null, des = null;
        if (args.length != 3 || !(src = new File(args[1])).exists() || (src.isDirectory() && (des = new File(args[2])).isFile())) {
            System.err.println("copy error, args is invalid.");
            return;
        } 
        copy(src, des);
        System.out.println("copy: copy ["+src.getPath()+"] to ["+des.getPath()+"] done.");
    }
    
    private static List<String> delList;
    //删除文件
    private static void del(File file) {
        try {
            if (file == null || !file.exists()) return;
            if (file.isDirectory() && file.list() != null && file.list().length > 0) {
                File[] files = file.listFiles();
                for (File _file : files) {
                    if (delList == null || !delList.contains(_file.getAbsolutePath())) {
                        del(_file);
                    }
                }
                if (delList == null || !delList.contains(file.getAbsolutePath())) {
                    delete(file);
                }
            } else {
                delete(file);
            }
        } catch (Exception e) {
            System.err.println("delete file["+file.getAbsolutePath()+"] failed.");
        }
    }
    
    private static void delete(File file) {
        if (file == null || !file.exists())   return;
        if (!file.delete()) {
            System.gc();
            file.delete();
        }
        if (delList != null) {
            delList.add(file.getAbsolutePath());
        }
    }
    
    //拷贝文件
    private static void copy(File src, File des) throws Exception {
        if (src == null || des == null)     return;
        if (src.isDirectory()) {
            File[] files = src.listFiles();
            for (File file : files) {
                copy(file, new File(des.getAbsolutePath() + File.separator + file.getName()));
            }
        } else {
            des.getParentFile().mkdirs();
            try (InputStream is = new FileInputStream(src);
                    OutputStream os = new FileOutputStream(des);) {
                byte[] bs = new byte[2048];
                int len = -1;
                while ((len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }
    
}