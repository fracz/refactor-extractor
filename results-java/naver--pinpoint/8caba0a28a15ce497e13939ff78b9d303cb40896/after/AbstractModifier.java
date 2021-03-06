package com.profiler.modifier;

import com.profiler.logging.Logger;

import javassist.*;

import java.net.URL;
import java.net.URLClassLoader;

public abstract class AbstractModifier implements Modifier {

	private static final Logger logger = Logger.getLogger(AbstractModifier.class);

	public byte[] addBeforeAfterLogics(ClassPool classPool, String javassistClassName) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			CtMethod[] methods = cc.getDeclaredMethods();

			for (CtMethod method : methods) {
				if (!method.isEmpty()) {
					String methodName = method.getName();
					CtClass[] params = method.getParameterTypes();
					StringBuilder sb = new StringBuilder();

					if (params.length != 0) {
						int paramsLength = params.length;

						for (int loop = paramsLength - 1; loop > 0; loop--) {
							sb.append(params[loop].getName()).append(",");
						}
						// sb.substring(0, sb.length()-2);
					}
					method.insertBefore("{System.out.println(\"*****" + javassistClassName + "." + methodName + "(" + sb + ") is started.\");}");
					method.insertAfter("{System.out.println(\"*****" + javassistClassName + "." + methodName + "(" + sb + ") is finished.\");}");
				} else {
					logger.warn(method.getLongName() + " is empty !!!!!");
				}
			}

			CtConstructor[] constructors = cc.getConstructors();

			for (CtConstructor constructor : constructors) {

				if (!constructor.isEmpty()) {
					CtClass[] params = constructor.getParameterTypes();
					StringBuilder sb = new StringBuilder();

					if (params.length != 0) {
						int paramsLength = params.length;
						for (int loop = paramsLength - 1; loop > 0; loop--) {
							sb.append(params[loop].getName()).append(",");
						}
						// sb.substring(0, sb.length()-2);
					}

					constructor.insertBefore("{System.out.println(\"*****" + javassistClassName + " Constructor:Param=(" + sb + ") is started.\");}");
					constructor.insertAfter("{System.out.println(\"*****" + javassistClassName + " Constructor:Param=(" + sb + ") is finished.\");}");
				} else {
					logger.warn(constructor.getLongName() + " is empty !!!!!");
				}
			}
			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public void printClassConvertComplete(String javassistClassName) {
		logger.info("%s class is converted.", javassistClassName);
	}

	protected void checkLibrary(ClassPool classPool, String javassistClassName, ClassLoader classLoader) {
		// TODO Util로 뽑을까?
		boolean findClass = findClass(classPool, javassistClassName);
		if (findClass) {
			return;
		}
		loadClassLoaderLibraries(classPool, classLoader);

	}

	public boolean findClass(ClassPool classPool, String javassistClassName) {
		// TODO 원래는 get인데. find는 ctclas를 생성하지 않아 변경. 어차피 아래서 생성하기는 함. 유효성 여부 확인
		// 필요
		URL url = classPool.find(javassistClassName);
		if (url == null) {
			return false;
		}
		return true;
	}

	private void loadClassLoaderLibraries(ClassPool classPool, ClassLoader classLoader) {
		if (classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			// TODO classLoader가 가지고 있는 전체 리소스를 모두 로드해야 되는것인지? 테스트 케이스 만들어서
			// 확인해봐야 할듯.
			URL[] urlList = urlClassLoader.getURLs();
			for (URL tempURL : urlList) {
				String filePath = tempURL.getFile();
				try {
					classPool.appendClassPath(filePath);
					// TODO 여기서 로그로 class로더를 찍어보면 어떤 clasdLoader에서 로딩되는지 알수 있을거
					// 것같음.
					// 만약 한개만 로딩해도 된다면. return true 할것
					// log("Loaded "+filePath+" library.");

				} catch (NotFoundException e) {
				}
			}
		}
	}
}