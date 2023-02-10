package io.github.portlek.realmformat.modifier;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public final class Transformer implements ClassFileTransformer {

  private static final boolean DEBUG = Boolean.getBoolean("clsmDebug");

  private static final String MODIFIER_CORE_FILE_NAME = "realm-format-modifier-core.txt";

  private static final String MODIFIER_LIST_FILE_NAME = "list.yaml";

  private static final String MODIFIER_TEMP_FILE_NAME = "realm-format-modifier-core";

  private static final Pattern PATTERN = Pattern.compile("^(\\w+)\\s*\\((.*?)\\)\\s*@(.+?\\.txt)$");

  private static final Map<String, Map<String, Change[]>> VERSION_CHANGES = new HashMap<>();

  private static final Yaml YAML = new Yaml();

  private Set<String> filesChecked = new HashSet<>();

  @Nullable
  private String version;

  @SneakyThrows
  public static void premain(final String agentArgs, final Instrumentation instrumentation) {
    final var modifierApiPath = Files.createTempFile(Transformer.MODIFIER_TEMP_FILE_NAME, ".jar");
    @Cleanup
    final var inputStream = Objects.requireNonNull(
      Transformer.class.getResourceAsStream("/" + Transformer.MODIFIER_CORE_FILE_NAME),
      "File '%s' not found!".formatted(Transformer.MODIFIER_CORE_FILE_NAME)
    );
    @Cleanup
    final var outputStream = new FileOutputStream(modifierApiPath.toFile());
    final var buf = new byte[8192];
    int length;
    while ((length = inputStream.read(buf)) > 0) {
      outputStream.write(buf, 0, length);
    }
    instrumentation.appendToSystemClassLoaderSearch(new JarFile(modifierApiPath.toFile()));
    instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(modifierApiPath.toFile()));
    instrumentation.addTransformer(new Transformer());
    @Cleanup
    final var fileStream = Objects.requireNonNull(
      Transformer.class.getResourceAsStream("/" + Transformer.MODIFIER_LIST_FILE_NAME),
      "File '%s' not found!".formatted(Transformer.MODIFIER_LIST_FILE_NAME)
    );
    final var yaml = new Yaml();
    @Cleanup
    final var reader = new InputStreamReader(fileStream);
    final var versionData = yaml.<Map<String, Object>>load(reader);
    for (final var version : versionData.keySet()) {
      final var data = (Map<String, Object>) versionData.get(version);
      for (final var originalClass : data.keySet()) {
        final var optional = originalClass.startsWith("__optional__");
        final var cls = originalClass.substring(optional ? 12 : 0);
        final var changeList = (List<String>) data.get(originalClass);
        final var changeArray = new Change[changeList.size()];
        for (var i = 0; i < changeList.size(); i++) {
          final var changeText = changeList.get(i);
          final var matcher = Transformer.PATTERN.matcher(changeText);
          if (!matcher.find()) {
            System.err.printf("Invalid change '%s' on class %s.%n", changeText, cls);
            System.exit(1);
            return;
          }
          final var methodName = matcher.group(1);
          final var paramsString = matcher.group(2).trim();
          final var parameters = paramsString.isEmpty()
            ? new String[0]
            : matcher.group(2).split(",");
          final var location = matcher.group(3);
          @Cleanup
          final var changeStream = Objects.requireNonNull(
            Transformer.class.getResourceAsStream("/" + location),
            "File '%s' not found!".formatted(location)
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
              final var newChanges = new Change[old.length + changeArray.length];
              System.arraycopy(old, 0, newChanges, 0, old.length);
              System.arraycopy(changeArray, 0, newChanges, old.length, changeArray.length);
              return newChanges;
            }
          );
      }
    }
  }

  @NotNull
  private static String nmsVersion(@NotNull final String minecraftVersion) {
    return switch (minecraftVersion) {
      case "1.18.2" -> "v1_18_R2";
      case "1.19", "1.19.1" -> "v1_19_R1";
      case "1.19.2", "1.19.3" -> "v1_19_R2";
      default -> throw new UnsupportedOperationException(minecraftVersion);
    };
  }

  private static byte@NotNull[] readAllBytes(@NotNull final InputStream stream) throws IOException {
    @Cleanup
    final var byteStream = new ByteArrayOutputStream();
    final var buffer = new byte[4096];
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
    final var changes = Transformer.VERSION_CHANGES.get(this.version);
    if (changes == null) {
      return null;
    }
    if (className == null) {
      return null;
    }
    if (!changes.containsKey(className)) {
      return null;
    }
    final var fixedClassName = className.replace("/", ".");
    if (Transformer.DEBUG) {
      System.out.printf("Applying changes for class %s%n", fixedClassName);
    }
    try {
      final var pool = ClassPool.getDefault();
      pool.appendClassPath(new LoaderClassPath(loader));
      pool.appendClassPath(new LoaderClassPath(Transformer.class.getClassLoader()));
      final var ctClass = pool.get(fixedClassName);
      for (final var change : changes.get(className)) {
        try {
          final var methods = ctClass.getDeclaredMethods(change.methodName());
          var found = false;
          main:for (final var method : methods) {
            final var params = method.getParameterTypes();
            if (params.length != change.parameters().length) {
              continue;
            }
            for (var i = 0; i < params.length; i++) {
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
    final var location = protectionDomain.getCodeSource().getLocation();
    if (!location.getProtocol().equals("file")) {
      return false;
    }
    final var filePath = location.getFile();
    if (this.filesChecked.contains(filePath)) {
      return false;
    }
    try {
      @Cleanup
      final var zipFile = new ZipFile(filePath);
      final var entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final var zipEntry = entries.nextElement();
        if (!zipEntry.isDirectory() && zipEntry.getName().equals("version.json")) {
          final var contents = Transformer.readAllBytes(zipFile.getInputStream(zipEntry));
          final var versionInfo = Transformer.YAML.<Map<String, String>>load(
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

  private record Change(
    @NotNull String methodName,
    @NotNull String@NotNull[] parameters,
    @NotNull String content,
    boolean optional
  ) {}
}
