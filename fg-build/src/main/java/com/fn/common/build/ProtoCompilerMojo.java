package com.fn.common.build;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * 绑定到maven的generate-sources生命周期，
 * 配置成功后，使用 mvn generate-source 即可编译proto文件
 *
 * <pre>
 * {@code
 * <plugin>
 *   <groupId>com.fn.common</groupId>
 *   <artifactId>fn-build</artifactId>
 *   <version>${version}</version>
 *   <executions>
 *     <execution>
 *       <goals>
 *         <goal>compile-proto</goal>
 *       </goals>
 *       <configuration>
 *         <protoSourceRoot>${project.basedir}/src/main/proto</protoSourceRoot>
 *       </configuration>
 *     </execution>
 *   </executions>
 * </plugin>
 * }
 * </pre>
 *
 * @author fomin
 * @date 2019-12-07
 */
@Mojo(name = "compile-proto", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public final class ProtoCompilerMojo extends AbstractMojo {

    /**
     * 存放 proto 文件的工程目录，由用户指定
     */
    @Parameter(alias = "protoSourceRoot", required = true)
    private File protoSourceRoot;

    /**
     * maven 项目对象，由 maven 在运行时注入
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * 此插件的所有依赖，由 maven 在运行时注入，用于生成启动 WireCompiler 的 classpath
     */
    @Parameter(defaultValue = "${plugin.artifacts}", required = true, readonly = true)
    private List<Artifact> pluginDependencies;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            String protoRoot = protoSourceRoot.getAbsolutePath();
            String generatedSourceRoot = project.getBasedir().getAbsolutePath() + "/target/generated-sources/proto/java";

            List<File> protoFiles = new ArrayList<>();
            collectProtoFiles(protoSourceRoot, protoFiles);

            String[] protoNames = protoFiles.stream()
                    .map(File::getAbsolutePath)
                    .map(f -> f.substring(protoRoot.length()))
                    .map(f -> f.startsWith(File.separator) ? f.substring(1) : f)
                    .toArray(String[]::new);

            String classpath = pluginDependencies.stream().map(a -> a.getFile().getAbsolutePath()).collect(joining(File.pathSeparator));

            CommandLine cmd = new CommandLine("java");
            cmd.addArgument("-classpath");
            cmd.addArgument(classpath);
            cmd.addArgument("com.squareup.wire.WireCompiler");
            cmd.addArgument("--proto_path=" + protoRoot);
            cmd.addArgument("--java_out=" + generatedSourceRoot);
            cmd.addArguments(protoNames);

            getLog().info("Compiling " + Arrays.toString(protoNames));
            getLog().debug("Compiler classpath: " + classpath);

            Executor exec = new DefaultExecutor();
            exec.setStreamHandler(createStreamHandler());
            exec.setExitValue(0);
            exec.execute(cmd);

            project.addCompileSourceRoot(generatedSourceRoot);

        } catch (MojoFailureException e) {
            throw e;

        } catch (ExecuteException e) {
            throw new MojoFailureException("Compile protos failed!");

        } catch (Throwable e) {
            getLog().error(e);
            throw new MojoExecutionException("Unexpected exception.", e);
        }
    }

    private void collectProtoFiles(File directory, List<File> protoFiles) throws MojoFailureException {
        File[] files = directory.listFiles();

        if (files == null) throw new MojoFailureException(directory + " is not a directory!");

        for (File file : files) {
            if (file.isDirectory()) {
                collectProtoFiles(file, protoFiles);

            } else if (file.getName().endsWith(".proto")) {
                protoFiles.add(file);
            }
        }
    }

    private ExecuteStreamHandler createStreamHandler() {
        OutputStream out = new org.apache.commons.exec.LogOutputStream() {
            @Override
            protected void processLine(String line, int logLevel) {
                getLog().debug(line);
            }
        };

        OutputStream err = new LogOutputStream() {
            @Override
            protected void processLine(String line, int logLevel) {
                getLog().error(line);
            }
        };

        return new PumpStreamHandler(out, err);
    }
}
