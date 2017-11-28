package com.github.taixiongliu.hapi.route;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * <b>Class scanner</b>
 * 
 * @author taixiong.liu
 * 
 */
public class ClassScanner {
	public ClassScanner() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * <b>get all classes in package</b>
	 * @param packageName e.g:com.xxx.xxx
	 * @return array class
	 */
    public static List<Class<?>> getClassesWithPackageName(String packageName) {
    	List<Class<?>> clazzList = new ArrayList<Class<?>>();
    	
    	//get current thread ClassLoader context
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        try {
            Enumeration<URL> enumeration = classLoader.getResources(path);
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                clazzList.addAll(selectClasses(new File(url.getFile()),packageName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clazzList;
    }
    
    private static List<Class<?>> selectClasses(File file, String packageName) {
    	List<Class<?>> list = new ArrayList<Class<?>>();
        if (!file.exists()) {
            return list;
        }
        // dir files
        File[] files = file.listFiles();
        for (File f : files) {
            if(f.isDirectory()){
            	if(f.getName().contains(".")){
            		continue;
            	}
            	// recursion 
            	List<Class<?>> arrayList = selectClasses(f, packageName + "." + f.getName());
                list.addAll(arrayList);
                continue;
            }
            if(!f.getName().endsWith(".class")){
            	continue;
            }
            try {
                // class not include suffix '.class'
                list.add(Class.forName(packageName + '.' + f.getName().substring(0, f.getName().length() - 6)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
