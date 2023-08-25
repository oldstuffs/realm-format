package io.github.portlek.realmformat.modifier;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javassist.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public final class Transformer implements ClassFileTransformer {

    private static final boolean DEBUG = Boolean.getBoolean("clsmDebug");

    private static final String MODIFIER_CORE_FILE_NAME = "realm-format-modifier-core.txt";

    private static final String MODIFIER_LIST_FILE_NAME = "list.yaml";

    private static final String MODIFIER_TEMP_FILE_NAME = "realm-format-modifier-core";

    private static final Pattern PATTERN = Pattern.compile(
        "^(\\w+)\\s*\\((.*?)\\)\\s*@(.+?\\.txt)$"
    );

    private static final Map<String, Map<String, Change[]>> VERSION_CHANGES = new HashMap<>();

    private static final Yaml YAML = new Yaml();

    private Set<String> filesChecked = new HashSet<>();

    @Nullable
    private String version;

    @SneakyThrows
    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        final Path modifierApiPath = Files.createTempFile(
            Transformer.MODIFIER_TEMP_FILE_NAME,
            ".jar"
        );
        @Cleanup
        final InputStream inputStream = Objects.requireNonNull(
            Transformer.class.getResourceAsStream("/" + Transformer.MODIFIER_CORE_FILE_NAME),
            String.format("File '%s' not found!", Transformer.MODIFIER_CORE_FILE_NAME)
        );
        @Cleanup
        final FileOutputStream outputStream = new FileOutputStream(modifierApiPath.toFile());
        final byte[] buf = new byte[8192];
        int length;
        while ((length = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, length);
        }
        instrumentation.appendToSystemClassLoaderSearch(new JarFile(modifierApiPath.toFile()));
        instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(modifierApiPath.toFile()));
        instrumentation.addTransformer(new Transformer());
        @Cleanup
        final InputStream fileStream = Objects.requireNonNull(
            Transformer.class.getResourceAsStream("/" + Transformer.MODIFIER_LIST_FILE_NAME),
            String.format("File '%s' not found!", Transformer.MODIFIER_LIST_FILE_NAME)
        );
        final Yaml yaml = new Yaml();
        @Cleanup
        final InputStreamReader reader = new InputStreamReader(fileStream);
        final Map<String, Object> versionData = yaml.load(reader);
        for (final String version : versionData.keySet()) {
            final Map<String, Object> data = (Map<String, Object>) versionData.get(version);
            for (final String originalClass : data.keySet()) {
                final boolean optional = originalClass.startsWith("__optional__");
                final String cls = originalClass.substring(optional ? 12 : 0);
                final List<String> changeList = (List<String>) data.get(originalClass);
                final Change[] changeArray = new Change[changeList.size()];
                for (int i = 0; i < changeList.size(); i++) {
                    final String changeText = changeList.get(i);
                    final Matcher matcher = Transformer.PATTERN.matcher(changeText);
                    if (!matcher.find()) {
                        System.err.printf("Invalid change '%s' on class %s.%n", changeText, cls);
                        System.exit(1);
                        return;
                    }
                    final String methodName = matcher.group(1);
                    final String paramsString = matcher.group(2).trim();
                    final String[] parameters = paramsString.isEmpty()
                        ? new String[0]
                        : matcher.group(2).split(",");
                    final String location = matcher.group(3);
                    @Cleanup
                    final InputStream changeStream = Objects.requireNonNull(
                        Transformer.class.getResourceAsStream("/" + location),
                        String.format("File '%s' not found!", location)
                    );
                    changeArray[i] =
                    new Change(
                        methodName,
                        parameters,
                        new String(Transformer.readAllBytes(changeStream), StandardCharsets.UTF_8),
                        optional
                    );
                }
                if (Transformer.DEBUG) {
                    System.out.printf("Loaded %s changes for class %s.%n", changeArray.length, cls);
                }
                Transformer.VERSION_CHANGES
                    .computeIfAbsent(version, k -> new HashMap<>())
                    .compute(
                        cls,
                        (__, old) -> {
                            if (old == null) {
                                return changeArray;
                            }
                            final Change[] newChanges = new Change[old.length + changeArray.length];
                            System.arraycopy(old, 0, newChanges, 0, old.length);
                            System.arraycopy(
                                changeArray,
                                0,
                                newChanges,
                                old.length,
                                changeArray.length
                            );
                            return newChanges;
                        }
                    );
            }
        }
    }

    @NotNull
    private static String nmsVersion(@NotNull final String minecraftVersion) {
        switch (minecraftVersion) {
            case "1.18.2":
                return "v1_18_R2";
            case "1.19":
            case "1.19.1":
                return "v1_19_R1";
            case "1.19.2":
            case "1.19.3":
                return "v1_19_R2";
            case "1.19.4":
                return "v1_19_R3";
            default:
                throw new UnsupportedOperationException(minecraftVersion);
        }
    }

    private static byte@NotNull[] readAllBytes(@NotNull final InputStream stream)
        throws IOException {
        @Cleanup
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];
        int readLen;
        while ((readLen = stream.read(buffer)) != -1) {
            byteStream.write(buffer, 0, readLen);
        }
        return byteStream.toByteArray();
    }

    @Override
    public byte@Nullable[] transform(
        final ClassLoader loader,
        final String className,
        final Class<?> classBeingRedefined,
        final ProtectionDomain protectionDomain,
        final byte[] classfileBuffer
    ) {
        if (this.version == null && !this.findVersion(protectionDomain)) {
            return null;
        }
        final Map<String, Change[]> changes = Transformer.VERSION_CHANGES.get(this.version);
        if (changes == null) {
            return null;
        }
        if (className == null) {
            return null;
        }
        if (!changes.containsKey(className)) {
            return null;
        }
        final String fixedClassName = className.replace("/", ".");
        if (Transformer.DEBUG) {
            System.out.printf("Applying changes for class %s%n", fixedClassName);
        }
        try {
            final ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new LoaderClassPath(loader));
            pool.appendClassPath(new LoaderClassPath(Transformer.class.getClassLoader()));
            final CtClass ctClass = pool.get(fixedClassName);
            for (final Change change : changes.get(className)) {
                try {
                    final CtMethod[] methods = ctClass.getDeclaredMethods(change.methodName());
                    boolean found = false;
                    main:for (final CtMethod method : methods) {
                        final CtClass[] params = method.getParameterTypes();
                        if (params.length != change.parameters().length) {
                            continue;
                        }
                        for (int i = 0; i < params.length; i++) {
                            if (!change.parameters()[i].trim().equals(params[i].getName())) {
                                continue main;
                            }
                        }
                        found = true;
                        try {
                            method.insertBefore(change.content());
                        } catch (final CannotCompileException ex) {
                            if (!change.optional()) {
                                throw ex;
                            }
                        }
                        break;
                    }
                    if (!found && !change.optional()) {
                        throw new NotFoundException("Unknown method " + change.methodName());
                    }
                } catch (final CannotCompileException ex) {
                    throw new CannotCompileException("Method " + change.methodName(), ex);
                }
            }
            return ctClass.toBytecode();
        } catch (final NotFoundException | CannotCompileException | IOException ex) {
            System.err.printf("Failed to override methods from class %s.%n", fixedClassName);
            ex.printStackTrace();
        }
        return null;
    }

    private boolean findVersion(@NotNull final ProtectionDomain protectionDomain) {
        final URL location = protectionDomain.getCodeSource().getLocation();
        if (!location.getProtocol().equals("file")) {
            return false;
        }
        final String filePath = location.getFile();
        if (this.filesChecked.contains(filePath)) {
            return false;
        }
        try {
            @Cleanup
            final ZipFile zipFile = new ZipFile(filePath);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory() && zipEntry.getName().equals("version.json")) {
                    final byte[] contents = Transformer.readAllBytes(
                        zipFile.getInputStream(zipEntry)
                    );
                    final Map<String, String> versionInfo = Transformer.YAML.load(
                        new String(contents, StandardCharsets.UTF_8)
                    );
                    this.version = Transformer.nmsVersion(versionInfo.get("id"));
                    this.filesChecked = null;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (this.version == null) {
                this.filesChecked.add(filePath);
            }
        }
        return this.version != null;
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    private static final class Change {

        @NotNull
        private final String methodName;

        @NotNull
        private final String@NotNull[] parameters;

        @NotNull
        private final String content;

        private final boolean optional;

        private Change(
            @NotNull final String methodName,
            @NotNull final String@NotNull[] parameters,
            @NotNull final String content,
            final boolean optional
        ) {
            this.methodName = methodName;
            this.parameters = parameters;
            this.content = content;
            this.optional = optional;
        }
    }
}
