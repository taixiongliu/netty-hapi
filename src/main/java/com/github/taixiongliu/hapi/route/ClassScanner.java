package com.github.taixiongliu.hapi.route;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
                String protocol = url.getProtocol();
                if("file".equals(protocol)){
                	clazzList.addAll(selectClasses(new File(url.getFile()),packageName));
                	continue;
                }
                if("jar".equals(protocol)){
                	String jarPath = url.getFile();
                	if(url.getFile().contains("!")){
                		jarPath = url.getFile().split("!")[0];
                	}
                	//substring 'file:'
                	jarPath = jarPath.substring(5, jarPath.length());
                	clazzList.addAll(getClasssFromJarFile(jarPath,path));
                	continue;
                }
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
    
	private static List<Class<?>> getClasssFromJarFile(String jarPath, String packagePath) {
		List<Class<?>> clazzs = new ArrayList<Class<?>>();

		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (jarFile == null) {
			return clazzs;
		}

		List<JarEntry> jarEntryList = new ArrayList<JarEntry>();

		Enumeration<JarEntry> ee = jarFile.entries();
		while (ee.hasMoreElements()) {
			JarEntry entry = (JarEntry) ee.nextElement();
			// filter package and class file
			if (entry.getName().startsWith(packagePath) && entry.getName().endsWith(".class")) {
				jarEntryList.add(entry);
			}
		}
		for (JarEntry entry : jarEntryList) {
			String className = entry.getName().replace('/', '.');
			className = className.substring(0, className.length() - 6);

			try {
				clazzs.add(Thread.currentThread().getContextClassLoader().loadClass(className));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return clazzs;
	}
}
