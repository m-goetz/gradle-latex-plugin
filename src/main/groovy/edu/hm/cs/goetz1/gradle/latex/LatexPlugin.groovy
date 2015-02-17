/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.hm.cs.goetz1.gradle.latex

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.GradleException
import groovy.io.FileType

/**
 * Plugin for Gradle to execute LaTeX builds.
 *
 * The plugin provides incremental builds and biblatex execution by using biber.
 *
 * @author: Maximilian Götz
 *
 */
class LatexPlugin implements Plugin<Project> {

	Project project

    Task inkscapeSvg
	Task pdflatexPreBuild
	Task biber
	Task pdflatex
	Task cleanPdflatex

	File outputDirectory
    File svgOutputDirectory
    File[] svgInputFiles

	void apply(Project project) {
		project.extensions.create("latex", LatexPluginExtension)
		this.project = project
		createAllTasks()

		project.afterEvaluate {
            if (project.latex.svgOutputDir == null) {
                project.latex.svgOutputDir = "${project.latex.outputDir}svg/"
            }
                    
			ensureExistingOutputDirectory()
			ensureExistingMainFilename()
            
            svgInputFiles = findSvgInputFiles()
            inkscapeSvg.inputs.files(svgInputFiles)
            inkscapeSvg.outputs.dir(svgOutputDirectory)
            inkscapeSvg << inkscapeSvgTaskAction

			pdflatexPreBuild.configure pdflatexPreBuildConfig
			pdflatexPreBuild.configure standardLogging

			biber.configure biberConfig
			biber.configure standardLogging

			pdflatex.configure pdflatexConfig
			pdflatex.configure standardLogging

			cleanPdflatex.delete outputDirectory.listFiles()
		}
	}

	private void createAllTasks() {
        inkscapeSvg = project.tasks.create("inkscapeSvg")
        inkscapeSvg.setDescription("Compiles scalable vector graphics (svg) into pdf_tex format.")
        inkscapeSvg.setGroup("LaTeX")
		pdflatexPreBuild = createLatexTask("pdflatexPreBuild",
				"Produces a pdf from a latex document by executing pdflatex once.")
        pdflatexPreBuild.dependsOn(inkscapeSvg)
		biber = createLatexTask("biber",
				"Runs biber to process the bibliography entries in the latex document.")
		biber.dependsOn(pdflatexPreBuild)
		pdflatex = createLatexTask("pdflatex",
				"Produces the final pdf from a latex document by executing pdflatex a second time.")
		pdflatex.dependsOn(biber)
		cleanPdflatex = project.tasks.create("cleanPdflatex", Delete.class)
		cleanPdflatex.setDescription("Deletes the output of the pdflatex task.")
		cleanPdflatex.setGroup("LaTeX")
	}

	private Task createLatexTask(String name, String description) {
		def latexTask = project.tasks.create(name, Exec.class)
		latexTask.setDescription(description)
		latexTask.setGroup("LaTeX")
		return latexTask
	}

	private void ensureExistingOutputDirectory() {
		outputDirectory = new File("${project.projectDir}/${project.latex.outputDir}")
		outputDirectory.mkdirs()
        svgOutputDirectory = new File("${project.projectDir}/${project.latex.svgOutputDir}")
        svgOutputDirectory.mkdirs()
        svgOutputDirectory.mkdir()
	}

	private void ensureExistingMainFilename() {
		if (project.latex.mainFilename == null) {
			project.latex.mainFilename = "${project.name}"
		}
	}
    
    private File[] findSvgInputFiles() {
        def list = []
        def svgDirectory = new File("${project.projectDir}/${project.latex.svgDir}")
        svgDirectory.eachFileRecurse(FileType.FILES) { file ->
            if (file.name.endsWith('.svg')) {
                list << file
            }
        }
        return list
    }
    
    private final Closure inkscapeSvgTaskAction = {
        def svgDestination = "${svgOutputDirectory}"
        ProcessBuilder pb;
        svgInputFiles.each { File file ->
            def subPath = file.path - project.projectDir - new File(project.latex.svgDir).path - file.name
            if (subPath.length() >= 3) {
                subPath = subPath[2..-1]
            }
            else {
                subPath = ""
            }
            def fileNameWithoutExt = file.name.replaceFirst(~/\.[^\.]+$/, '')
            def wholePath = "${svgDestination}\\${subPath}"
            def wholePathFile = new File(wholePath)
            while (!wholePathFile.exists()) {
                wholePathFile.mkdirs()
                wholePathFile.mkdir()
                Thread.sleep(300)
            }
            pb = new ProcessBuilder(["cmd", "/c", "inkscape -D -z ${file.path} --export-pdf=${wholePath}${fileNameWithoutExt}.pdf --export-latex".toString()]);
            Process proc = pb.start()
            proc.waitFor()
        }
    }

	private final Closure pdflatexPreBuildConfig = {
		commandLine "pdflatex",
				"-output-directory=${outputDirectory}",
				"-synctex=1", "-interaction=nonstopmode",
				"${project.latex.mainFilename}.tex"
		workingDir("${project.latex.sourceDir}")
		inputs.dir("${project.latex.sourceDir}")
		outputs.file("${outputDirectory}/${project.latex.mainFilename}.bcf")
	}

	private final Closure biberConfig = {
		def inputFile = new File("${outputDirectory}/${project.latex.mainFilename}.bcf")
		commandLine "biber", "--output_directory=${outputDirectory}",
				"--logfile=${project.latex.mainFilename}","${inputFile}"
		workingDir("${project.latex.sourceDir}")
		inputs.file("${outputDirectory}/${project.latex.mainFilename}.bcf")
        inputs.dir("${project.latex.bibFilesDir}")
		outputs.files("${outputDirectory}/${project.latex.mainFilename}.bbl",
				"${outputDirectory}/${project.latex.mainFilename}.blg")
		onlyIf { inputFile.exists() }
	}

	private final Closure pdflatexConfig = {
		commandLine "pdflatex",
				"-output-directory=${outputDirectory}",
				"-synctex=1", "-interaction=nonstopmode",
				"${project.latex.mainFilename}.tex"
		workingDir("${project.latex.sourceDir}")
		inputs.dir("${project.latex.sourceDir}")
		outputs.dir("${outputDirectory}")
	}

	private final Closure standardLogging = {
        if (logger.isEnabled(LogLevel.INFO)) {
            logging.captureStandardOutput LogLevel.INFO
            logging.captureStandardError  LogLevel.ERROR
        }
        else {
            standardOutput = new ByteArrayOutputStream()
            errorOutput = standardOutput
        }
        ignoreExitValue = true
        
        doLast {
            if (execResult.exitValue != 0) {
                println(standardOutput.toString())
                throw new GradleException("Could not compile LaTeX document; see process output above.")
            }
        }
	}
}


