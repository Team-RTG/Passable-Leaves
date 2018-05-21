package teamrtg.passableleaves.asm;

import net.minecraft.launchwrapper.IClassTransformer;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import teamrtg.passableleaves.asm.PassableLeavesTransformer.Transforms.Fields;
import teamrtg.passableleaves.asm.PassableLeavesTransformer.Transforms.Methods;

/**
 * This class was originally written by HellFirePvP for the Appalachia addon for RTG.
 * It was extracted from Appalachia by WhichOnesPink so that it could be a standalone mod.
 * The complete source code for this mod can be found on GitHub.
 * Class: PassableLeavesTransformer
 * @author HellFirePvP
 * @since 2017.02.12
 * @author srs-bsns
 * @since 2017.10.04
 */
public class PassableLeavesTransformer implements IClassTransformer {

    static final class Transforms {
        private Transforms() {}
        private static final String[] names = new String[0];
        enum Methods {
            // Names must be the deobfuscated method names, These, and/or the obfuscated names, may change over time.
            getCollisionBoundingBox(
                "func_180646_a",
                "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;"
            ),
            isPassable(
                "func_176205_b",
                "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Z"
            ),
            onEntityCollidedWithBlock(
                "func_180634_a",
                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V"
            );
            private final String obfName;
            private final String desc;
            Methods(String obfName, String desc) {
                this.obfName = obfName;
                this.desc = desc;
                ArrayUtils.add(names, name().toLowerCase());
            }
            String getName() { return PassableLeavesCore.isDeobf() ? name() : obfName; }
            String getDesc() { return desc; }
            static boolean contains(final String value) { return ArrayUtils.contains(names, value.toLowerCase()); }
        }
        enum Fields {
            NULL_AABB("field_185506_k", "Lnet/minecraft/util/math/AxisAlignedBB;");
            private final String obfName;
            private final String desc;
            Fields(String obfName, String desc) {
                this.obfName = obfName;
                this.desc = desc;
            }
            String getName() { return PassableLeavesCore.isDeobf() ? this.name() : this.obfName; }
            String getDesc() { return this.desc; }
        }
    }

    public PassableLeavesTransformer() {
        PassableLeavesCore.LOGGER.info("PassableLeavesTransformer Initialized");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {

        if(!transformedName.equals("net.minecraft.block.BlockLeaves")) { return basicClass; }

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        //Check sanity before doing anything.
        //Potentially also the place where you want to check for configs.
        //Keep in mind this is called well before preInit
        for (MethodNode mn : node.methods) {
            if (Methods.contains(mn.name)) {
                PassableLeavesCore.LOGGER.error("net.minecraft.block.BlockLeaves was modified by another Class transformer. Not doing changes.");
                return basicClass;
            }
        }

        // getCollisionBox method construction
        PassableLeavesCore.LOGGER.info("Patching net.minecraft.block.BlockLeaves with an override for net.minecraft.block.Block#getCollisionBox");
        LabelNode startLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();
        MethodNode getCollisionBox = new MethodNode(Opcodes.ACC_PUBLIC, Methods.getCollisionBoundingBox.getName(), Methods.getCollisionBoundingBox.getDesc(), null, new String[0]);
        getCollisionBox.instructions = new InsnList();
        getCollisionBox.instructions.add(startLabel);
        getCollisionBox.instructions.add(new LineNumberNode(150, startLabel)); //Required

        AnnotationNode nullable = new AnnotationNode("Ljavax/annotation/Nullable;");
        nullable.values = Lists.newArrayList();
        getCollisionBox.visibleAnnotations = Lists.newArrayList(nullable);

        //TODO keep track of parameter changes between minecraft updates!
        LocalVariableNode lvnThis = new LocalVariableNode("this", "Lnet/minecraft/block/BlockLeaves;", null, startLabel, endLabel, 0);
        LocalVariableNode lvnPState = new LocalVariableNode("inState", "Lnet/minecraft/block/state/IBlockState;", null, startLabel, endLabel, 1);
        LocalVariableNode lvnPWorld = new LocalVariableNode("inWorld", "Lnet/minecraft/world/IBlockAccess;", null, startLabel, endLabel, 2);
        LocalVariableNode lvnPPos = new LocalVariableNode("inPos", "Lnet/minecraft/util/math/BlockPos;", null, startLabel, endLabel, 3);
        getCollisionBox.localVariables = Lists.newArrayList(lvnThis, lvnPState, lvnPWorld, lvnPPos);

        getCollisionBox.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/block/BlockLeaves", Fields.NULL_AABB.getName(), Fields.NULL_AABB.getDesc()));
        getCollisionBox.instructions.add(new InsnNode(Opcodes.ARETURN));
        getCollisionBox.instructions.add(endLabel);

        node.methods.add(getCollisionBox);


        // isPassable method construction
        PassableLeavesCore.LOGGER.info("Patching net.minecraft.block.BlockLeaves with an override for net.minecraft.block.Block#isPassable");
        startLabel = new LabelNode();
        endLabel = new LabelNode();
        MethodNode isPassable = new MethodNode(Opcodes.ACC_PUBLIC, Methods.isPassable.getName(), Methods.isPassable.getDesc(), null, new String[0]);
        isPassable.visibleAnnotations = Lists.newArrayList();
        isPassable.instructions = new InsnList();
        isPassable.instructions.add(startLabel);
        isPassable.instructions.add(new LineNumberNode(160, startLabel)); //Required

        //TODO keep track of parameter changes between minecraft updates!
        lvnThis = new LocalVariableNode("this", "Lnet/minecraft/block/BlockLeaves;", null, startLabel, endLabel, 0);
        lvnPState = new LocalVariableNode("inWorld", "Lnet/minecraft/world/IBlockAccess;", null, startLabel, endLabel, 1);
        lvnPPos = new LocalVariableNode("inPos", "Lnet/minecraft/util/math/BlockPos;", null, startLabel, endLabel, 2);
        isPassable.localVariables = Lists.newArrayList(lvnThis, lvnPState, lvnPPos);

        isPassable.instructions.add(new InsnNode(Opcodes.ICONST_1));
        isPassable.instructions.add(new InsnNode(Opcodes.IRETURN));
        isPassable.instructions.add(endLabel);

        node.methods.add(isPassable);


        // onEntityCollidedWithBlock method construction
        PassableLeavesCore.LOGGER.info("Patching net.minecraft.block.BlockLeaves with an override for net.minecraft.block.Block#onEntityCollidedWithBlock");
        startLabel = new LabelNode();
        endLabel = new LabelNode();
        MethodNode onEntityCollidedWithBlock = new MethodNode(Opcodes.ACC_PUBLIC, Methods.onEntityCollidedWithBlock.getName(), Methods.onEntityCollidedWithBlock.getDesc(), null, new String[0]);
        onEntityCollidedWithBlock.visibleAnnotations = Lists.newArrayList();
        onEntityCollidedWithBlock.instructions = new InsnList();
        onEntityCollidedWithBlock.instructions.add(startLabel);
        onEntityCollidedWithBlock.instructions.add(new LineNumberNode(170, startLabel)); //Required
        lvnThis = new LocalVariableNode("this", "Lnet/minecraft/block/BlockLeaves;", null, startLabel, endLabel, 0);
        lvnPWorld = new LocalVariableNode("inWorld", "Lnet/minecraft/world/World;", null, startLabel, endLabel, 1);
        lvnPPos = new LocalVariableNode("inPos", "Lnet/minecraft/util/math/BlockPos;", null, startLabel, endLabel, 2);
        lvnPState = new LocalVariableNode("inState", "Lnet/minecraft/block/state/IBlockState;", null, startLabel, endLabel, 3);
        LocalVariableNode lvnPEntity = new LocalVariableNode("inEntity", "Lnet/minecraft/entity/Entity;", null, startLabel, endLabel, 4);
        onEntityCollidedWithBlock.localVariables = Lists.newArrayList(lvnThis, lvnPWorld, lvnPPos, lvnPState, lvnPEntity);

        // Add VarInstNodes to match the desc of net.minecraft.block.Block#onEntityCollidedWithBlock
        onEntityCollidedWithBlock.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        onEntityCollidedWithBlock.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        onEntityCollidedWithBlock.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        onEntityCollidedWithBlock.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
        onEntityCollidedWithBlock.instructions.add(
            // This has to point to PassableLeaves#onEntityCollidedWithLeaves
            new MethodInsnNode(Opcodes.INVOKESTATIC, "teamrtg/passableleaves/PassableLeaves", "onEntityCollidedWithLeaves", Methods.onEntityCollidedWithBlock.getDesc(), false)
        );
        onEntityCollidedWithBlock.instructions.add(new InsnNode(Opcodes.RETURN));
        onEntityCollidedWithBlock.instructions.add(endLabel);

        node.methods.add(onEntityCollidedWithBlock);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        basicClass = writer.toByteArray();

        return basicClass;
    }

}
