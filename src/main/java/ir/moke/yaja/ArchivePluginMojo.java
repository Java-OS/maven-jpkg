package ir.moke.yaja;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Mojo(name = "archive", defaultPhase = LifecyclePhase.PACKAGE)
public class ArchivePluginMojo extends AbstractMojo {
    private static final String ANSI_RESET = "\u001B[0m";
    public static final String WHITE_BOLD = "\033[1;37m";
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Jos module name
     */
    @Parameter(name = "name", required = true, readonly = true)
    private String name;
    /**
     * Jos module version
     */
    @Parameter(name = "version", required = true, readonly = true)
    private String version;

    /**
     * Jos module maintainer
     */
    @Parameter(name = "maintainer", readonly = true)
    private String maintainer;

    /**
     * Jos module url
     */
    @Parameter(name = "url", readonly = true)
    private String url;

    /**
     * Jos module description
     */
    @Parameter(name = "description", readonly = true)
    private String description;

    /**
     * Jos's dependencies of this module
     */
    @Parameter(name = "dependencies", readonly = true)
    private Dependency[] dependencies;

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        Path targetDirectory = getTargetDirectory();
        //TODO: change suffix zip to yaja
        Path targetYajaFilePath = targetDirectory.resolve(name + "-" + version + ".yaja");
        Path manifestPath = targetDirectory.resolve("manifest.yaml");
        YajaArchive yajaArchive = createYajaArchiveObject();
        YamlUtils.writeToFile(manifestPath.toFile(), yajaArchive);

        List<Path> filesToZip = new ArrayList<>();
        filesToZip.add(manifestPath);
        filesToZip.add(project.getArtifact().getFile().toPath());

        Set<DefaultArtifact> depArtifactList = project.getDependencyArtifacts();
        for (DefaultArtifact dep : depArtifactList) {
            if (dep.getScope().equals("compile")) filesToZip.add(dep.getFile().toPath());
        }
        try {
            ArchiveUtils.zipFile(targetYajaFilePath, filesToZip);
            getLog().info("Jos archive generated: " + WHITE_BOLD + targetYajaFilePath.toAbsolutePath() + ANSI_RESET);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path getTargetDirectory() {
        Path targetDir = Paths.get(project.getBuild().getDirectory() + "/jos-module");
        if (!Files.exists(targetDir)) {
            try {
                Files.createDirectory(targetDir);
            } catch (IOException e) {
                getLog().error(e.getMessage());
            }
        }
        return targetDir;
    }

    private YajaArchive createYajaArchiveObject() {
        YajaArchive archive = new YajaArchive();
        archive.setName(name);
        archive.setVersion(version);
        archive.setMaintainer(maintainer);
        archive.setDescription(description);
        archive.setUrl(url);
        if (dependencies != null) {
            List<String> list = Arrays.stream(dependencies).map(item -> item.getName() + ":" + item.getVersion()).toList();
            archive.setDependencies(list.toArray(String[]::new));
        }
        return archive;
    }
}
