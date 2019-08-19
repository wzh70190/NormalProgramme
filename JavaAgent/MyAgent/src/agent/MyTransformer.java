package agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ��ⷽ����ִ��ʱ��
 */
public class MyTransformer implements ClassFileTransformer {

    final static String prefix = "\nlong startTime = System.currentTimeMillis();\n";
    final static String postfix = "\nlong endTime = System.currentTimeMillis();\n";

    // ������ķ����б�
    final static Map<String, List<String>> methodMap = new HashMap<String, List<String>>();

    public MyTransformer() {
        add("alibaba.TimeTest.sayHello");
        add("alibaba.TimeTest.sayHello2");
    }

    private void add(String methodString) {
        String className = methodString.substring(0, methodString.lastIndexOf("."));
        String methodName = methodString.substring(methodString.lastIndexOf(".") + 1);
        List<String> list = methodMap.get(className);
        if (list == null) {
            list = new ArrayList<String>();
            methodMap.put(className, list);
        }
        list.add(methodName);
    }

    // ��д�˷���
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/", ".");
        if (methodMap.containsKey(className)) {
            // �жϼ��ص�class�İ�·���ǲ�����Ҫ��ص���
            CtClass ctclass = null;
            try {
                ClassPool cp = new ClassPool();
                cp.appendSystemPath();
                ctclass = ClassPool.getDefault().get(className);// ʹ��ȫ��,����ȡ���ֽ�����<ʹ��javassist>
                for (String methodName : methodMap.get(className)) {
                    String outputStr = "\nSystem.out.println(\"this method " + methodName
                            + " cost:\" +(endTime - startTime) +\"ms.\");";

                    CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);// �õ��ⷽ��ʵ��
                    String newMethodName = methodName + "$old";// �¶���һ��������������sayHello$old
                    ctmethod.setName(newMethodName);// ��ԭ���ķ��������޸�

                    // �����µķ���������ԭ���ķ���������Ϊԭ��������
                    CtMethod newMethod = CtNewMethod.copy(ctmethod, methodName, ctclass, null);

                    // �����µķ�����
                    StringBuilder bodyStr = new StringBuilder();
                    bodyStr.append("{");
                    bodyStr.append(prefix);
                    bodyStr.append(newMethodName + "($$);\n");// ����ԭ�д��룬������method();($$)��ʾ���еĲ���
                    bodyStr.append(postfix);
                    bodyStr.append(outputStr);
                    bodyStr.append("}");
                    //{long startTime = System.currentTimeMillis();
                    // sayHello$old($$);
                    //long endTime = System.currentTimeMillis();
                    //\nSystem.out.println(\"this method " + methodName+ " cost:\" +(endTime - startTime) +\"ms.\");
                    newMethod.setBody(bodyStr.toString());// �滻�·���
                    ctclass.addMethod(newMethod);// �����·���
                    System.err.println(outputStr);
                }
                return ctclass.toBytecode();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
}