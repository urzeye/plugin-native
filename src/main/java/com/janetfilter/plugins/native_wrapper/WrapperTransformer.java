package com.janetfilter.plugins.native_wrapper;

import com.janetfilter.core.Environment;
import com.janetfilter.core.commons.DebugInfo;
import com.janetfilter.core.models.FilterRule;
import com.janetfilter.core.plugin.MyTransformer;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class WrapperTransformer implements MyTransformer {
    private final Environment env;
    private final List<FilterRule> rules;

    public WrapperTransformer(Environment env, List<FilterRule> rules) {
        this.env = env;
        this.rules = rules;
    }

    @Override
    public String getHookClassName() {
        return null;
    }

    @Override
    public byte[] transform(String className, byte[] classBytes, int order) throws Exception {
        if (!classMatched(className)) {
            return classBytes;
        }

        ClassReader reader = new ClassReader(classBytes);
        ClassNode node = new ClassNode(ASM5);
        reader.accept(node, 0);

        String originName;
        String prefix = env.getNativePrefix();
        List<MethodNode> methodNodes = new ArrayList<>();
        for (MethodNode mn : node.methods) {
            if (0 == (mn.access & ACC_NATIVE)) {
                continue;
            }

            mn.name = prefix + (originName = mn.name);

            MethodNode nmn = new MethodNode(ASM5);
            nmn.access = mn.access & ~ACC_NATIVE;
            nmn.name = originName;
            nmn.desc = mn.desc;
            nmn.signature = mn.signature;
            nmn.exceptions = mn.exceptions;
            nmn.parameters = mn.parameters;
            nmn.attrs = mn.attrs;
            nmn.visibleAnnotations = mn.visibleAnnotations;
            nmn.invisibleAnnotations = mn.invisibleAnnotations;
            nmn.visibleTypeAnnotations = mn.visibleTypeAnnotations;
            nmn.invisibleTypeAnnotations = mn.invisibleTypeAnnotations;
            nmn.annotationDefault = mn.annotationDefault;
            nmn.visibleParameterAnnotations = mn.visibleParameterAnnotations;
            nmn.invisibleParameterAnnotations = mn.invisibleParameterAnnotations;
            nmn.instructions.insert(generateInsnList(nmn, className, mn.name));
            methodNodes.add(nmn);
        }

        node.methods.addAll(methodNodes);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);

        return writer.toByteArray();
    }

    private InsnList generateInsnList(MethodNode mn, String className, String wrappedMethodName) {
        InsnList list = new InsnList();

        int slot = 0, index = 1;
        boolean isNonStatic = 0 == (mn.access & ACC_STATIC);

        if (isNonStatic) {
            list.add(new VarInsnNode(ALOAD, slot++));
        }

        char type;
        while (')' != (type = mn.desc.charAt(index))) {
            switch (type) {
                case 'B':
                case 'C':
                case 'I':
                case 'S':
                case 'Z':
                    list.add(new VarInsnNode(ILOAD, slot++));
                    break;
                case 'F':
                    list.add(new VarInsnNode(FLOAD, slot++));
                    break;
                case 'D':
                    list.add(new VarInsnNode(DLOAD, slot));
                    slot += 2;
                    break;
                case 'J':
                    list.add(new VarInsnNode(LLOAD, slot));
                    slot += 2;
                    break;
                case 'L':
                case '[':
                    list.add(new VarInsnNode(ALOAD, slot++));
                    break;
            }

            index = nextDesc(mn.desc, index);
        }

        list.add(new MethodInsnNode(isNonStatic ? INVOKEVIRTUAL : INVOKESTATIC, className, wrappedMethodName, mn.desc, false));
        list.add(new InsnNode(getReturnOpcode(mn.desc.charAt(index + 1))));

        return list;
    }

    private int nextDesc(String desc, int index) {
        int ret = index + 1;
        switch (desc.charAt(index)) {
            case 'L':
                while (desc.charAt(ret) != ';') {
                    ret++;
                }

                return ret + 1;
            case '[':
                return nextDesc(desc, ret);
            default:
                return ret;
        }
    }

    private int getReturnOpcode(char returnDesc) {
        switch (returnDesc) {
            case 'B':
            case 'C':
            case 'I':
            case 'S':
            case 'Z':
                return IRETURN;
            case 'F':
                return FRETURN;
            case 'D':
                return DRETURN;
            case 'J':
                return LRETURN;
            case 'L':
            case '[':
                return ARETURN;
            case 'V':
                return RETURN;
            default:
                throw new RuntimeException("invalid return type");
        }
    }

    private boolean classMatched(String className) {
        for (FilterRule rule : rules) {
            if (rule.test(className)) {
                DebugInfo.output("Native wrapper: " + className + ", rule: " + rule);
                return true;
            }
        }

        return false;
    }
}
