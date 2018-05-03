package com.pennassurancesoftware.oipa.upload;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.testng.annotations.Test;

import com.jcabi.immutable.Array;
import com.pennassurancesoftware.oipa.upload.Upload.Support.Filter.FileEndsWith;
import com.pennassurancesoftware.oipa.upload.aether.AetherCoordinates;
import com.pennassurancesoftware.oipa.upload.aether.AetherDeployments;

public class TestUploadArtifact {

	private Predicate<File> byWarFile() {
		return (file) -> file.getName().endsWith(".war");
	}

	private AetherDeployments deployments() {
		return new AetherDeployments.Default(Repos.pasExtRepo());
	}

	private File oipaFolder() {
		return new File("build/oipa");
	}

	private Upload.Oipa.Version oipaVersion(File warFile) {
		return new Upload.Oipa.App(warFile).version();
	}

	private Consumer<File> printVersion() {
		return (warFile) -> System.out.println(oipaVersion(warFile));
	}

	@Test(groups = "integration", enabled = true)
	public void test_upload_oipa() throws Exception {
		Upload.Oipa.from(oipaFolder()).uploads().forEach(u -> u.upload());
	}

	@Test(groups = "integration", enabled = false)
	public void test_All() throws Exception {
		warFiles().forEach(uploadOracleAll());
	}

	@Test(groups = "integration", enabled = false)
	public void test_classesOnly() throws Exception {
		warFiles().forEach(uploadOracleClasses());
	}

	@Test(groups = "integration", enabled = false)
	public void test_libsOnly() throws Exception {
		warFiles().forEach(uploadOracleLibs());
	}

	@Test(groups = "integration", enabled = false)
	public void test_Version() throws Exception {
		warFiles().forEach(printVersion());
	}

	@Test(groups = "integration", enabled = false)
	public void test_WarOnly() throws Exception {
		warFiles().forEach(uploadOracleWar());
	}

	private Consumer<File> uploadOracleAll() {
		return uploadOracleAll(deployments());
	}

	private Consumer<File> uploadOracleAll(final AetherDeployments deployments) {
		return uploadOracleClasses(deployments).andThen(uploadOracleLibs(deployments))
				.andThen(uploadOracleWar(deployments));
	}

	private Consumer<File> uploadOracleClasses() {
		return uploadOracleClasses(deployments());
	}

	private Consumer<File> uploadOracleClasses(final AetherDeployments deployments) {
		return (file) -> new Upload.War.Classes(file, deployments, AetherCoordinates.empty().withArtifactId("pas.web")
				.withGroupId("com.adminserver").withVersion(oipaVersion(file).toString())).upload();
	}

	private Consumer<File> uploadOracleLibs() {
		return uploadOracleLibs(deployments());
	}

	private Consumer<File> uploadOracleLibs(final AetherDeployments deployments) {
		return (file) -> new Upload.War.Libs(file, deployments)
				.where(new FileEndsWith(String.format("%s.jar", oipaVersion(file).toString()))).upload();
	}

	private Consumer<File> uploadOracleWar() {
		return uploadOracleWar(deployments());
	}

	private Consumer<File> uploadOracleWar(final AetherDeployments deployments) {
		return (file) -> new Upload._File(file, deployments, AetherCoordinates.empty().withArtifactId("PASJava")
				.withGroupId("com.adminserver").withPackaging("war").withVersion(oipaVersion(file).toString()))
						.upload();
	}

	private Stream<File> warFiles() {
		return warFiles(oipaFolder());
	}

	private Stream<File> warFiles(File folder) {
		return new Array<File>(folder.listFiles()).stream().filter(byWarFile());
	}
}
