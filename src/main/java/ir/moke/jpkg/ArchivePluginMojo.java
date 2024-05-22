package ir.moke.jpkg;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mojo(name = "archive", defaultPhase = LifecyclePhase.PACKAGE)
public class ArchivePluginMojo extends AbstractMojo {
    public static final String WHITE_BOLD = "\033[1;37m";
    private static final String ANSI_RESET = "\u001B[0m";

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}")
    private List<RemoteRepository> projectRepos;

    @Parameter(defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repoSession;

    /**
     * Jos module name
     */
    @Parameter(name = "name", required = true)
    private String name;
    /**
     * Jos module version
     */
    @Parameter(name = "version", required = true)
    private String version;

    /**
     * Jos module maintainer
     */
    @Parameter(name = "maintainer")
    private String maintainer;

    /**
     * Jos module url
     */
    @Parameter(name = "url")
    private String url;

    /**
     * Jos module description
     */
    @Parameter(name = "description")
    private String description;

    /**
     * Jos's dependencies of this module
     */
    @Parameter(name = "dependencies")
    private JosDependency[] dependencies;

    private static String getHash(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return path.toFile().getName() + " - " + DigestUtils.sha256Hex(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dependency> getArtifactsDependencies(MavenProject project, String scope) throws Exception {
        DefaultArtifact pomArtifact = new DefaultArtifact(project.getId());

        Dependency dependency = new Dependency(pomArtifact, scope);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        collectRequest.setRepositories(projectRepos);

        DependencyNode node = repositorySystem.collectDependencies(repoSession, collectRequest).getRoot();
        DependencyRequest projectDependencyRequest = new DependencyRequest(node, null);

        repositorySystem.resolveDependencies(repoSession, projectDependencyRequest);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg);

        return new ArrayList<>(nlg.getDependencies(true));
    }

    @Override
    public void execute() {
        try {
            List<Dependency> projectDependencies = getArtifactsDependencies(project, "provided");

            Path targetDirectory = getTargetDirectory();
            Path targetJpkgFilePath = targetDirectory.resolve(name + "-" + version + ".jpkg");
            Path manifestPath = targetDirectory.resolve("manifest.yaml");

            List<Path> filesToZip = new ArrayList<>();
            filesToZip.add(manifestPath);

            List<String> fileHashes = new ArrayList<>();
            for (Dependency dependency : projectDependencies) {
                Artifact artifact = dependency.getArtifact();
                Path path = artifact.getFile().toPath();
                filesToZip.add(path);
                fileHashes.add(getHash(path));
            }

            System.out.println("Files Hash : ");
            fileHashes.forEach(item -> System.out.println("- " + item));

            JpkgArchive jpkgArchive = createJpkgArchiveObject();
            jpkgArchive.setFiles(fileHashes);
            YamlUtils.writeToFile(manifestPath.toFile(), jpkgArchive);

            ArchiveUtils.zipFile(targetJpkgFilePath, filesToZip);
            getLog().info("Jos archive generated: " + WHITE_BOLD + targetJpkgFilePath.toAbsolutePath() + ANSI_RESET);
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

    private JpkgArchive createJpkgArchiveObject() {
        JpkgArchive archive = new JpkgArchive();
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
